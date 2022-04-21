

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import javax.jms.Queue;
import java.sql.SQLOutput;
import java.util.*;
import java.util.stream.Collectors;

public class Officer implements JMSConnection {
    private static List<Integer> priorityQueue;
    private static Map<Integer, Coin> mids = new HashMap<>();
    private static Map<Integer, Coin> highs = new HashMap<>();
    private static Map<Integer, Coin> lows = new HashMap<>();


    public static void main(String args[]) throws JMSException {
        System.out.println("Officer is spawned!");

        JMSFactory factory = new JMSFactory();

        Connection connection = factory.startConnection(CONNECTION_URL);
        Session session = factory.createSession(connection);
        Topic officerTopic = factory.createTopic(session, "officerTopic");
        WorkerCommunication communications = new WorkerCommunication(AMOUNT_OF_WORKERS);
        MessageConsumer consumer = factory.createConsumerQueue(session, "officer");


        List<Integer> nodesThatAreDoneWithSorting = new ArrayList<>();
        int amountOfNodesThatAreDoneWithSorting = 0;

        ArrayList<Integer> nodesThatAreDoneWithMerging = new ArrayList<Integer>();
        while (nodesThatAreDoneWithMerging.size() < AMOUNT_OF_WORKERS) {
            System.out.println("wachten op nodes die klaar zijn met mergen");
            WorkerTextMessage workerTextMessage = (WorkerTextMessage) ((ObjectMessage) consumer.receive()).getObject();
            if (workerTextMessage.getMessageType().equals("done_merging")) {
                nodesThatAreDoneWithMerging.add(Integer.parseInt(workerTextMessage.getMessage()));
            }
        }
        System.out.println("Officer konws everyone is done with merging.");
        WorkerCommunication workerCommunication = new WorkerCommunication(AMOUNT_OF_WORKERS);
        String[] mergedQueues = workerCommunication.createMergedQueues();

        /**
         * Special case in geval van slechts 2 nodes
         */
        if (AMOUNT_OF_WORKERS <= 2) { //Special case waarbij de hele lijst gesorteerd is na 1 keer mergen
            for (int i = 0; i < AMOUNT_OF_WORKERS; i++) {
                MessageProducer communicator = startNodeMessageServer(nodesThatAreDoneWithMerging.get(i), factory);
                communicator.send(session.createObjectMessage(new WorkerTextMessage("queue_name", mergedQueues[0])));
            }
            MessageConsumer mergedAlphaConsumer = factory.createConsumerQueue(session, "merged-Alpha");
            Node comletelySortedNode = sortAlphaQueueAndGet(mergedAlphaConsumer, session);

            System.out.println("The entire merge sort process is finished. This is the end result. ");
            comletelySortedNode.revealGenealogy();


            /**
             * Meer dan 2 nodes
             */
        } else {
            /**
             * Enkel gebruik maken van merged0, merged1 en merged-Alpha.
             * De gemergede resultaten door sturen naar QueueAlpha.
             * Methode aanmaken voor dinges vanaf regel 81. Deze merged de Alpha Merged naar 1 gesorteerde node.
             */
        }




    }
    public static Node combine(Node first, Node second){
        if(first.c.compareTo(second.c) > 0){
            first.setPrev(second);
            return first;
        }else{
            second.setPrev(first);
            return second;
        }
    }

    private static Node sortAlphaQueueAndGet(MessageConsumer alphaConsumer, Session session) throws JMSException {
        ArrayList<Node> sortedNodes = new ArrayList<>();
        int amountOfHeadsAndTailsOnAlphaQueue = getQueueSize(session,session.createQueue("merged-Alpha"));
        while (sortedNodes.size() < amountOfHeadsAndTailsOnAlphaQueue) {
            sortedNodes.add(((CoinListMessage) ((ObjectMessage) alphaConsumer.receive()).getObject()).getNode());
        }

        Node completelySortedNode;
        List<Coin> completelySortedCoins = new ArrayList<>();
        LinkedList skr = new LinkedList();
        if (sortedNodes.size() > 1) {
            System.out.println("=========================");
            completelySortedNode = skr.merge(sortedNodes.get(0), sortedNodes.get(1));
//            completelySortedNode = combine(sortedNodes.get(0), sortedNodes.get(1));
//            if (sortedNodes.get(0).c.compareTo(sortedNodes.get(1).c) > 0) {
//                completelySortedCoins.addAll(sortedNodes.get(1).revealGenealogy());
//                completelySortedCoins.addAll(sortedNodes.get(0).revealGenealogy());
//            } else {
//
//                completelySortedCoins.addAll(sortedNodes.get(0).revealGenealogy());
//                completelySortedCoins.addAll(sortedNodes.get(1).revealGenealogy());
//
//            }
//            completelySortedNode = new LinkedList().merge(sortedNodes.get(0), sortedNodes.get(1));
        } else {
            completelySortedNode = sortedNodes.get(0);



        }
        return completelySortedNode;
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

    private static MessageProducer startNodeMessageServer(Integer originNodeId, JMSFactory factory) throws JMSException {
        String nodeMessageServer = "message-server-" + originNodeId;
        Connection connection = factory.startConnection(CONNECTION_URL);
        Session session = factory.createSession(connection);
        return factory.createProducerQueue(session, nodeMessageServer);


    }
//    //Vertel tegen de workers waar ze hun gemergde elementen gaan opslaan
//    private static void sendMergeMessage(MessageProducer producer, Session session, int mergerQueueId, String headOrTail) throws JMSException {
//        ObjectMessage message = session.createObjectMessage();
//        //determine if the node will do head sorting or tail sorting
//
//
//        WorkerCommunication configMessage = new WorkerTextMessage(Message(headOrTail,headOrTail+mergerQueueId);
//        message.setObject(configMessage);
//        producer.send(message);
//    }

//    private static void sendHeadAndTailConfigToNode(Integer originNodeId, MessageProducer producer, Session session) throws JMSException {
//        ObjectMessage message = session.createObjectMessage();
//        //determine if the node will do head sorting or tail sorting
//        boolean isHeader = originNodeId % 2 == 0;
//
//        WorkerCommunication configMessage = new WorkerCommunication("destination", "merged"+originNodeId, AMOUNT_OF_WORKERS);
//        message.setObject(configMessage);
//        producer.send(message);
//    }


    public Officer(String topic) {


    }

    public void generateMergerQueues() {
        //Hierin geeft de officer aan welke node head/tail zijn.
    }

    public void retrieveHiLoAndPivotsFromNodes() {

    }

    private void assignMergeTask(int nodeId1, int nodeId2) {

    }


}
