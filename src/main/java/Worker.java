
import javax.jms.*;
import javax.jms.Queue;
import java.util.*;


public class Worker implements JMSConnection {

    static Coin sendBack = null;
    public static void main(String[] args) throws JMSException {
        int workerId = 0;
        int numClients = 2;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--workerId")) {
                i++;
                workerId = Integer.parseInt(args[i]);
            } else if (args[i].equals("--numClients")) {
                i++;
                numClients = Integer.parseInt(args[i]);
            }
        }
        boolean leftWorker = workerId % 2 == 0; //Workers met een even workerId zijn leftworkers
        //Maak een nieuwe instance van een zelfgeschreven LinkedList class
        LinkedList myCoinlist = new LinkedList();

        //Genereer (random) coinObjecten en voeg ze toe per stuk toe aan een lege LinkedList instance; First-in-first-out
        for (Coin c : generateCoinObjects()) {
            myCoinlist.addTail(c);
        }
        //Sorteer de lijst d.m.v. een recursief Merge Sort algoritme
        myCoinlist.sortList();
        System.out.println();
        System.out.println(leftWorker ? "Left worker: " : "Right worker: ");
        myCoinlist.printList();

        //Zet de connectie op met JMS
        JMSFactory jmsFactory = new JMSFactory();
        Connection connection = jmsFactory.startConnection(CONNECTION_URL);
        Session session = jmsFactory.createSession(connection);

        //Bericht de officer dat ik klaar ben sorteren, m.a.w. klaar om te mergen met andere gesorteerde lijst.
        Topic officerTopic = jmsFactory.createTopic(session, "officerTopic");
        MessageProducer topicProducer = session.createProducer(officerTopic);
        topicProducer.send(session.createObjectMessage(new WorkerTextMessage("done_sorting", "worker" + workerId)));

        //Elke worker is een producer omdat hij zijn eigen data heeft die verzonden moet worden naar andere workers
        //Deze data bestaat uit TextMessages, maar ook uit ObjectMessages waar Coin objecten in staan
//        MessageProducer producer = jmsFactory.createProducerQueue(session, "head0");
//        producer.send(session.createObjectMessage(myCoinlist.giveHeadElementAndRemove()));

        LinkedList receivedCoins = new LinkedList();

        boolean done = false;

        System.out.println(workerId);
        MessageProducer messageProducer;
        MessageConsumer messageConsumer;
        if (leftWorker) {
            messageProducer = jmsFactory.createProducerQueue(session, "tail0");
            messageConsumer = jmsFactory.createConsumerQueue(session, "head0");
        } else {
            messageProducer = jmsFactory.createProducerQueue(session, "head0");
            messageConsumer = jmsFactory.createConsumerQueue(session, "tail0");
        }
        Coin receivedCoin = null;
        while (!done) {
            if (leftWorker) {
                Coin savedTail = myCoinlist.giveTailElement().c;

                //Stuur de grootste Coin van mijn eigen gesorteerde lijst weg
//                MessageProducer producer1 = jmsFactory.createProducerQueue(session, "tail0");
                messageProducer.send(session.createObjectMessage(myCoinlist.giveTailElementAndRemove()));

                //Ontvang van een andere worker zijn laagste Coin
//                MessageConsumer consumer1 = jmsFactory.createConsumerQueue(session, "head0");
                System.out.println(workerId + ") Gonna wait for coins");
                receivedCoin = (Coin) ((ObjectMessage) messageConsumer.receive()).getObject();
                System.out.println(workerId + ")Just received coins");
                //send grootste getallen naar successor.
                //ontvang kleinste getallen van succesor.


            } else {
                Coin savedHead = myCoinlist.giveHeadElement().c;
                //Stuur de laagste Coin van mijn eigen gesorteerde lijst weg
//                MessageProducer producer1 = jmsFactory.createProducerQueue(session, "head0");
                messageProducer.send(session.createObjectMessage(myCoinlist.giveHeadElementAndRemove()));

                //Ontvang van een andere worker zijn laagste Coin
//                MessageConsumer consumer1 = jmsFactory.createConsumerQueue(session, "tail0");
                System.out.println(workerId + ")Gonna wait for coins");
                receivedCoin = (Coin) ((ObjectMessage) messageConsumer.receive()).getObject();
                System.out.println(workerId + ")Just received coins");
                //Kleinste getallen weg sturen naar predecessor. Wilt graag grootste
                //ontvang grootste getallen van predessor


            }
            System.out.println(workerId + ") Coinlist size: " + myCoinlist.size());


            done = processExchangedValues(receivedCoin, myCoinlist, leftWorker);

        }
        boolean sentBack = false;
        while(!sentBack)
        if (leftWorker && sendBack != null) {
            messageProducer.send(session.createObjectMessage(sendBack));
            myCoinlist.addTail((Coin) ((ObjectMessage) messageConsumer.receive()).getObject());
            sentBack = true;
        } else if(!leftWorker && sendBack != null){
            messageProducer.send(session.createObjectMessage(sendBack));
            myCoinlist.addHead((Coin) ((ObjectMessage) messageConsumer.receive()).getObject());
            sentBack = true;
        }
        myCoinlist.printList();
        System.out.println("DONE WITH SORTING BOOOYAAHH!");

        //Nu heeft de leftworker enkel getallen (coins) die kleiner zijn dan alle getallen van de rightworker
        //Tijd om de officer te berichten dat alles goed verlopen is
        MessageProducer officerProducer = jmsFactory.createProducerQueue(session, "officer");
        officerProducer.send(session.createObjectMessage(new WorkerTextMessage("done_merging", String.valueOf(workerId))));

        //Worker ontvangt van officer naar welke queue hij zijn gemergde lijst moet sturen
        MessageConsumer messageServerConsumer = jmsFactory.createConsumerQueue(session, "message-server-" + workerId);
        String queueName = ((WorkerTextMessage) ((ObjectMessage) messageServerConsumer.receive()).getObject()).getMessage();
        System.out.println("Worker heeft queue ontvangen van officer");

        //Stuur linkedList naar aangeweszen queue
        MessageProducer coinListProducer = jmsFactory.createProducerQueue(session, queueName);
        coinListProducer.send(session.createObjectMessage(new CoinListMessage(myCoinlist.giveHeadElement())));


    }


    /**
     * @param session
     * @param queue
     * @return het aantal enqueuede berichten op @queue
     */
    private static int getQueueSize(Session session, Queue queue) {
        int count = 0;
        try {
            QueueBrowser browser = session.createBrowser(queue);
            Enumeration elems = browser.getEnumeration();
            while (elems.hasMoreElements()) {
                elems.nextElement();
                count++;
            }
        } catch (JMSException ex) {
            ex.printStackTrace();
        }
        return count;
    }

    /**
     * NODE A; 2 3 5 6 7 12
     * NODE B: 1 4 8 9 10 11
     * <p>
     * <p>
     * 1st round: A[12] --> B[1]   A: 1 2 3 5 6 7    B: 4 8 9 10 11 12
     * 2nd round: A[7] --> B[4]    A: 1 2 3 4 5 6    B: 7 8 9 10 11 12
     * 3rd round: A[6] --> B[8] //Zal niet accepteren omdat
     * <p>
     * expected A: 1 2 3 4 5 6
     * expected B: 7 8 9 10 11 12
     */


    private static boolean processExchangedValues(Coin receivedCoin, LinkedList myList, boolean isLeftWorker) {

        System.out.println( isLeftWorker ? "Left worker: Coin received: " + receivedCoin.toString() : "Right Worker: Coin received: " + receivedCoin.toString());
        if (isLeftWorker) {
            System.out.println("Left worker own tail element is: " + myList.giveTailElement().c.toString());
            if (myList.giveTailElement().c.compareTo(receivedCoin) < 0) {
                System.out.println("Left worker tail element is smaller than received coin");
                //Betekent dat de ontvangen Coin niet thuis hoort in deze lijst. Stuur de Coin terug naar de sender
                sendBack = receivedCoin; //Om variabelen te besparen voegen we de coin toe aan de Tail. Dit element zal
                //direct uit de lijst gehaald worden als processExchanged() true returned. Dit betekent namelijk ten alle tijde
                //dat het hoogste elemement van mijn eigen lijst lager is dan het laagste elmeent in een andere lijst
                return true; //Lijst is gesorteerd
            }
            myList.insert(receivedCoin); //ontvangen head //TODO change to insertHead

        } else {
            System.out.println("Right worker own tail element is: " + myList.giveHeadElement().c.toString());
            if (myList.giveHeadElement().c.compareTo(receivedCoin) > 0) {
                System.out.println("Rigth worker headElement is bigger than received coin");
                //Betekent dat de ontvangen Coin niet thuis hoort in deze lijst. Stuur de Coin terug naar de sender
                sendBack = receivedCoin; //Om variabelen te besparen voegen we de coin toe aan de head. Dit element zal
                //direct uit de lijst gehaald worden als processExchanged() true returned. Dit betekent namelijk ten alle tijde
                //dat het laagste elemement van mijn eigen lijst al hoger is dan het hoogste elmeent in een andere lijst
                return true; //Lijst is gesorteerd;
            }
            myList.insert(receivedCoin); //ontvangen tail //TODO change to insertTail

        }
        return false;

    }

    public static List<Coin> generateCoinObjects() {
        return new DataGenerator(AMOUNT_OF_ELEMENTS).generate();
    }

    public boolean isWorkerTextMessage(ObjectMessage objectMessage) {
        return objectMessage instanceof WorkerTextMessage;
    }


}
