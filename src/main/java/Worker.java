
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

        //Zet de connectie op met JMS
        JMSFactory jmsFactory = new JMSFactory();
        Connection connection = jmsFactory.startConnection(CONNECTION_URL);
        Session session = jmsFactory.createSession(connection);

        //Bericht de officer dat ik klaar ben sorteren, m.a.w. klaar om te mergen met andere gesorteerde lijst.
        Topic officerTopic = jmsFactory.createTopic(session, "officerTopic");
        MessageProducer topicProducer = session.createProducer(officerTopic);
        topicProducer.send(session.createObjectMessage(new WorkerTextMessage("done_sorting", "worker"+workerId)));

        //Elke worker is een producer omdat hij zijn eigen data heeft die verzonden moet worden naar andere workers
        //Deze data bestaat uit uit TextMessages, maar ook uit ObjectMessages waar Coin objecten in staan
        MessageProducer producer = jmsFactory.createProducerQueue(session, "head0");
        producer.send(session.createObjectMessage(myCoinlist.giveHeadElementAndRemove()));

        boolean done = false;
        while(!done){
            if(leftWorker){
                //send grootste getallen naar successor.
                //ontvang kleinste getallen van succesor.
            }else{
                //worker %2 == 0; Even getal, kleinste getallen weg sturen naar predecessor. Wilt graag grootste
                //ontvang grootste getallen van predessor
            }
//           done = processExchangedValues()

        }
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

    public static List<Coin> generateCoinObjects(){
        return new DataGenerator(AMOUNT_OF_ELEMENTS).generate();
    }

    public boolean isWorkerTextMessage(ObjectMessage objectMessage){
        return objectMessage instanceof WorkerTextMessage;
    }


}
