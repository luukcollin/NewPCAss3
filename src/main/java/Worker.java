
import javax.jms.*;
import javax.jms.Queue;
import java.util.*;


public class Worker implements JMSConnection{


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

        //Maak een nieuwe instance van een zelfgeschreven LinkedList class
        LinkedList myCoinlist = new LinkedList();

        //Genereer (random) coinObjecten en voeg ze toe per stuk toe aan een lege LinkedList instance; First-in-first-out
        for(Coin c : generateCoinObjects()){
            myCoinlist.addTail(c);
        }
        //Sorteer de lijst d.m.v. een recursief Merge Sort algoritme
        myCoinlist.sortList();
myCoinlist.printList();

        //Zet de connectie op met JMS
        JMSFactory jmsFactory = new JMSFactory();
        Connection connection = jmsFactory.startConnection(CONNECTION_URL);
        Session session = jmsFactory.createSession(connection);

        //Bericht de officer dat ik klaar ben sorteren, m.a.w. klaar om te mergen met andere gesorteerde lijst.
        Topic officerTopic = jmsFactory.createTopic(session, "officerTopic");
        MessageProducer topicProducer = session.createProducer(officerTopic);
        topicProducer.send(session.createObjectMessage(new WorkerTextMessage("done_sorting", "worker"+workerId)));

        //Elke worker is een producer omdat hij zijn eigen data heeft die verzonden moet worden naar andere workers
        //Deze data bestaat uit TextMessages, maar ook uit ObjectMessages waar Coin objecten in staan
//        MessageProducer producer = jmsFactory.createProducerQueue(session, "head0");
//        producer.send(session.createObjectMessage(myCoinlist.giveHeadElementAndRemove()));

        LinkedList receivedCoins = new LinkedList();

        boolean done = false;
        boolean leftWorker =  workerId % 2 == 0; //Workers met een even workerId zijn leftworkers
        System.out.println(workerId);
        MessageProducer messageProducer;
        MessageConsumer messageConsumer;
        if(leftWorker){
            messageProducer = jmsFactory.createProducerQueue(session, "tail0");
            messageConsumer = jmsFactory.createConsumerQueue(session, "head0");
        }else{
            messageProducer = jmsFactory.createProducerQueue(session, "head0");
            messageConsumer = jmsFactory.createConsumerQueue(session, "tail0");
        }
        Coin receivedCoin = null;
        while(!done){
            if(leftWorker){
                Coin savedTail = myCoinlist.giveTailElement().c;

                //Stuur de grootste Coin van mijn eigen gesorteerde lijst weg
//                MessageProducer producer1 = jmsFactory.createProducerQueue(session, "tail0");
                messageProducer.send(session.createObjectMessage(myCoinlist.giveTailElementAndRemove()));

                //Ontvang van een andere worker zijn laagste Coin
//                MessageConsumer consumer1 = jmsFactory.createConsumerQueue(session, "head0");
                System.out.println(workerId +  ") Gonna wait for coins");
                receivedCoin= (Coin) ((ObjectMessage) messageConsumer.receive()).getObject();
                System.out.println(workerId +  ")Just received coins");
                //send grootste getallen naar successor.
                //ontvang kleinste getallen van succesor.



            }else{
                Coin savedHead = myCoinlist.giveHeadElement().c;
                //Stuur de laagste Coin van mijn eigen gesorteerde lijst weg
//                MessageProducer producer1 = jmsFactory.createProducerQueue(session, "head0");
                messageProducer.send(session.createObjectMessage(myCoinlist.giveHeadElementAndRemove()));

                //Ontvang van een andere worker zijn laagste Coin
//                MessageConsumer consumer1 = jmsFactory.createConsumerQueue(session, "tail0");
                System.out.println(workerId +  ")Gonna wait for coins");
                receivedCoin = (Coin) ((ObjectMessage) messageConsumer.receive()).getObject();
                System.out.println(workerId +  ")Just received coins");
                //Kleinste getallen weg sturen naar predecessor. Wilt graag grootste
                //ontvang grootste getallen van predessor


            }
            System.out.println(workerId + ") Coinlist size: " + myCoinlist.size());


            done = processExchangedValues(receivedCoin, myCoinlist, leftWorker);

        }
        if(leftWorker){
            messageProducer.send(session.createObjectMessage(myCoinlist.giveTailElementAndRemove()));
            myCoinlist.addTail((Coin)((ObjectMessage)messageConsumer.receive()).getObject());
        }else{
            messageProducer.send(session.createObjectMessage(myCoinlist.giveHeadElementAndRemove()));
            myCoinlist.addHead((Coin)((ObjectMessage)messageConsumer.receive()).getObject());
        }
        myCoinlist.printList();
        System.out.println("DONE WITH SORTING BOOOYAAHH!");
        //Daarnaast zou het mooi zijn als dezelfde worker berichten kan consumeren van andere Workers,zodat deze samen
        //Head/Tail kunnen mergen.
        while(myCoinlist.giveHeadElement() != null) {
            MessageConsumer consumer = jmsFactory.createConsumerQueue(session, "tail0");
            Coin receivedTail = ((CoinMessage) ((ObjectMessage) consumer.receive()).getObject()).getMessage();
            //Als mijn eigen tail element kleiner is dan de ontvangen tail
            if(myCoinlist.giveTailElementAndRemove().compareTo(receivedTail) < 0){
                //Voeg deze dan toe aan mijn eigen tail
                myCoinlist.addTail(receivedTail);
            }


        }


        }

    /**
     *
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
     *
     * NODE A; 2 3 5 6 7 12
     * NODE B: 1 4 8 9 10 11
     *
     *
     * 1st round: A[12] --> B[1]   A: 1 2 3 5 6 7    B: 4 8 9 10 11 12
     * 2nd round: A[7] --> B[4]    A: 1 2 3 4 5 6    B: 7 8 9 10 11 12
     * 3rd round: A[6] --> B[8] //Zal niet accepteren omdat
     *
     * expected A: 1 2 3 4 5 6
     * expected B: 7 8 9 10 11 12
     *
     */


    private static boolean processExchangedValues(Coin receivedCoin,LinkedList myList, boolean isLeftWorker){

        System.out.println(isLeftWorker +  ", Coin given: "+ receivedCoin.toString());
        if(isLeftWorker){
            if( myList.giveTailElement().c.compareTo(receivedCoin) < 0) {
                //Betekent dat de ontvangen Coin niet thuis hoort in deze lijst. Stuur de Coin terug naar de sender
                myList.addTail(receivedCoin); //Om variabelen te besparen voegen we de coin toe aan de Tail. Dit element zal
                //direct uit de lijst gehaald worden als processExchanged() true returned. Dit betekent namelijk ten alle tijde
                //dat het hoogste elemement van mijn eigen lijst lager is dan het laagste elmeent in een andere lijst
                return true; //Lijst is gesorteerd
            }
            myList.insert(receivedCoin); //ontvangen head //TODO change to insertHead

        }else{
            if(myList.giveHeadElement().c.compareTo(receivedCoin) > 0){
                //Betekent dat de ontvangen Coin niet thuis hoort in deze lijst. Stuur de Coin terug naar de sender
                myList.addHead(receivedCoin); //Om variabelen te besparen voegen we de coin toe aan de head. Dit element zal
                //direct uit de lijst gehaald worden als processExchanged() true returned. Dit betekent namelijk ten alle tijde
                //dat het laagste elemement van mijn eigen lijst al hoger is dan het hoogste elmeent in een andere lijst
                return true; //Lijst is gesorteerd;
            }
            myList.insert(receivedCoin); //ontvangen tail //TODO change to insertTail

        }
        return false;

    }

    public static List<Coin> generateCoinObjects(){
        return new DataGenerator(AMOUNT_OF_ELEMENTS).generate();
    }

    public boolean isWorkerTextMessage(ObjectMessage objectMessage){
        return objectMessage instanceof WorkerTextMessage;
    }


}
