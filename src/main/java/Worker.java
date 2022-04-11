
import javax.jms.*;
import javax.jms.Queue;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;


public class Worker implements JMSConnection{


    public static void main(String[] args) throws JMSException {
        LinkedList myCoinlist = new LinkedList();

        List<Coin> listOfGeneratedCoins = new DataGenerator(10).generate();


        List<Coin> testCoins = new DataGenerator(10).generate();

        Coin testTail = testCoins.get(0);
        System.out.println("TestTail: "  + testTail.toString());

        Coin testHead = testCoins.get(1);
        System.out.println("Testhead: " + testHead.toString());

        for(Coin c : listOfGeneratedCoins){
            myCoinlist.addHead(c);
        }
        myCoinlist.printList();

        myCoinlist.addTail(testTail);
        myCoinlist.addHead(testHead);


        myCoinlist.printList();

        if(false) {
            int workerId = 0;
            int numClients = 4;
            JMSFactory factory = new JMSFactory();
            Connection connection = factory.startConnection(CONNECTION_URL);
            Session session = factory.createSession(connection);
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("--nodeId")) {
                    i++;
                    //Set workerid
                    workerId = Integer.parseInt(args[i]);
                } else if (args[i].equals("--numClients")) {
                    i++;
                    numClients = Integer.valueOf(args[i]);
                }
            }
            MessageConsumer consumer = factory.createConsumerQueue(session, ("message-server-" + workerId));


            // process command line arguments, decide to assume master role or worker role

            //Genereer lijst met met CoinObjecten
            List<Coin> unsortedElements = generateElements();
            System.out.println("Sorting the elements will start");
            //Start de timer, omdat het sorteren gaat beginnen vanaf heir
            Timer.start();
            //Sorteer de zojuist gegeneerde CoinObjecten
            List<Coin> sortedElements = sortUnsortedElements(unsortedElements);
            Timer.stop();
            System.out.println("Worker(" + workerId + ")" + ": Sorting the elements is done");
            System.out.println("I WOULD LIKE TO START MERGING");

            notifyOfficer(session, factory, workerId);

            MessageProducer coinProducer = null;
            MessageConsumer coinConsumer = null;
            Coin receivedCoin = null;

            boolean isHeader = false;
            //Wacht totdat de officer een reply stuurt
            while (coinProducer == null) {
                NodeMessage nodeMessage = (NodeMessage) ((ObjectMessage) consumer.receive()).getObject();
                //Bepaal of node zich gaat focussen op de head of de tail
                if (nodeMessage.getMessageType().equals("head")) {
                    //Maak een producerQueue aan om Coin elementen te kunenn sturen naar de toegewezen queue
                    coinProducer = factory.createProducerQueue(session, nodeMessage.getMessage());
                    isHeader = true;
                } else if (nodeMessage.getMessageType().equals("tail")) {
                    coinProducer = factory.createProducerQueue(session, nodeMessage.getMessage());
                }
            }
            //Verstuur de CoinObjecten via de messageProducer naar de queue
            //Ontvang ook een bericht van de andere node en vergelijk.
            while (sortedElements.size() > 0) {
                Coin headOrTail = giveHead(sortedElements); // isHeader ? giveHead(sortedElements) : giveTail(sortedElements);
                coinProducer.send(session.createObjectMessage(headOrTail));
                sortedElements.remove(headOrTail);
            }


            //=============== MERGING ==============================
            //Merge alles van Queue head0 met tail0 en head1 met tail1 etc.
            int i = 0;
            if (workerId == 1) {
                System.out.println("HELLO I'M WORKERID 1");


                MessageConsumer consumerHead = factory.createConsumerQueue(session, "head0");
                MessageConsumer consumerTail = factory.createConsumerQueue(session, "tail0");
                List<Coin> tempHead = new ArrayList<>();
                List<Coin> tempTail = new ArrayList<>();


                LinkedList headResult = new LinkedList();
                LinkedList tailResult = new LinkedList();


                Coin receivedHead = null;
                Coin receivedTail = null;


                int totalCoinsProcessed = 0;
                int totalCoinsToProcess = 200; //Decreased the amount for testing purposes
                int totalElementsInHead0Queue = getQueueSize(session, session.createQueue("head0"));
                int totalElementsInTail0Queue = getQueueSize(session, session.createQueue("tail0"));
                //Verwerk alle objecten in de queues
                while (totalCoinsProcessed < totalCoinsToProcess) {
                    System.out.println((tempHead.size() + tempTail.size()));

                    System.out.println("Tempead size: " + tempHead.size());
                    if (receivedHead == null && totalElementsInHead0Queue > 0) {
                        receivedHead = (Coin) ((ObjectMessage) consumerHead.receiveNoWait()).getObject();

                    }
                    System.out.println("Temptail size: " + tempTail.size());
                    if (receivedTail == null && totalElementsInTail0Queue > 0) {
                        receivedTail = (Coin) ((ObjectMessage) consumerTail.receiveNoWait()).getObject();
                        System.out.println("JUST RECEIVED TAILER");
                        System.out.println(receivedTail.toString());

                    }

                    //BUG: Op de een of andere manier blijft hij altijd zeggen dat elk 'receivedTail' kleiner is dan 'receivedHead'
                    if (receivedHead != null && receivedTail != null && (receivedTail.compareTo(receivedHead) < 0)) {
                        tempTail.add(receivedHead);
                        receivedHead = null;

                    } else if (receivedHead != null && receivedTail != null) {
                        tempHead.add(receivedTail);
                        receivedTail = null;
                    }
                    totalCoinsProcessed++;
                    i++;

                    System.out.println("i " + i);
                    totalElementsInHead0Queue = getQueueSize(session, session.createQueue("head0"));
                    ;
                    totalElementsInTail0Queue = getQueueSize(session, session.createQueue("tail0"));
                    ;

                }
                //Sla het overgebleven element op in de juiste lijst.
                if (receivedHead != null) {
                    tempHead.add(0, receivedHead);
                } else if (receivedTail != null) {
                    tempTail.add(receivedTail);
                }

                //Print resultaten.
                System.out.println("All headers: ");
                for (Coin c : tempHead) {
                    System.out.println(c.toString());
                }
                System.out.println("All tailers: ");
                for (Coin c : tempTail) {
                    System.out.println(c.toString());
                }
            }
        }

    }


    // Return the value at rear
    static Coin peek(Node fr) { return fr.c; }

    static boolean isEmpty(Node fr) { return (fr == null); }

//        //TODO modify that when sortedElements.size() < 0, send empty map.

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

    private static void notifyOfficer(Session session, JMSFactory factory, int workerId) throws JMSException {
        MessageProducer producer= factory.createProducerQueue(session, "officer");
        producer.send(session.createObjectMessage(new NodeMessage("done_sorting", String.valueOf(workerId))));
    }

    private static Coin giveHead(List<Coin> sortedElements){
        return sortedElements.get(0);
    }
     private static Coin giveTail(List<Coin> sortedElements){
        return sortedElements.get(sortedElements.size()-1);
    }


    //Stuur de tail naar de queue
    private static Coin giveHeadOrTail(List<Coin> sortedElements, String type){
       Coin c = null;
        if(type.equals("head")) {
            c= giveHead(sortedElements);
        }else if(type.equals("tail")){
            c = giveTail(sortedElements);
        }
        return c;
    }

    public static List<Coin> generateElements(){
        return new DataGenerator(AMOUNT_OF_ELEMENTS).generate();
    }

    public static List<Coin> sortUnsortedElements( List<Coin> unsortedElements){
        return new Sort().sort(unsortedElements);
    }

    public static ObjectMessage createMapMessage(int workerId, List<Coin> sortedElements, Session session) throws JMSException {
        ObjectMessage mapMessage = session.createObjectMessage();

        //Maak variabelen aan voor de objecten die in de Map komen.
        int lastIndex = sortedElements.size()-1;
        Coin lo = sortedElements.get(0);
        Coin hi =  sortedElements.get(lastIndex);
        Coin mid = sortedElements.get(lastIndex/2); //TOOD fix error??? Math.floor??

        //Plaats de coinobjecten in de Map
        Map<String, Coin> hilomid = new HashMap<>();
        hilomid.put("lo", lo);
        hilomid.put("mid", mid);
        hilomid.put("hi", hi);

        //Maak een wrapper voor de Map aan, zodat ook de workerId meegegeven kan worden.
        Map<Integer, Map<String, Coin>> actualMapMessage = new HashMap<>();
        actualMapMessage.put(workerId, hilomid);
        mapMessage.setObject((Serializable)actualMapMessage);
        return  mapMessage;
    }

}
