

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
        String[] messageServers = workerCommunication.createMessageServerQueueNames();

        /**
         * In geval van 4 Nodes:
         * 1. Merge 1 and 2, merge 3 and 4. [1: LEFT, 2: RIGHT, 3: LEFT, 4: RIGHT] //DONE step1
         * 2. Merge 1 and 3, merge 2 and 4. [1: LEFT, 2: LEFT, 3: RIGHT, 4: RIGHT] //DONE step2
         * 3. Merge 2 and 3. [1: LEFT, 2 left MID, 3 right MID, 4 RIGHT] step3
         */

        //Step 2: Merge 1 and 3. Merge 2 and 4.
        MessageProducer w1Communicator = factory.createProducerQueue(session, messageServers[0]);
        MessageProducer w2Communicator = factory.createProducerQueue(session, messageServers[1]);
        MessageProducer w3Communicator = factory.createProducerQueue(session, messageServers[2]);
        MessageProducer w4Communicator = factory.createProducerQueue(session, messageServers[3]);

        w1Communicator.send(session.createObjectMessage(new WorkerTextMessage("produce", "step2-odd-head")));
        w1Communicator.send(session.createObjectMessage(new WorkerTextMessage("consume", "step2-odd-tail")));
        w3Communicator.send(session.createObjectMessage(new WorkerTextMessage("produce", "step2-odd-tail")));
        w3Communicator.send(session.createObjectMessage(new WorkerTextMessage("consume", "step2-odd-head")));

        w2Communicator.send(session.createObjectMessage(new WorkerTextMessage("produce", "step2-even-head")));
        w2Communicator.send(session.createObjectMessage(new WorkerTextMessage("consume", "step2-even-tail")));
        w4Communicator.send(session.createObjectMessage(new WorkerTextMessage("consume", "step2-even-head")));
        w4Communicator.send(session.createObjectMessage(new WorkerTextMessage("produce", "step2-even-tail")));

        //Officer will wait again till Nodes are done with merging...
        ArrayList<Integer> nodesThatAreDoneWithMergingStep2 = new ArrayList<Integer>();
        while (nodesThatAreDoneWithMergingStep2.size() < AMOUNT_OF_WORKERS) {
            System.out.println("wachten op nodes die klaar zijn met mergen");
            WorkerTextMessage workerTextMessage = (WorkerTextMessage) ((ObjectMessage) consumer.receive()).getObject();
            if (workerTextMessage.getMessageType().equals("done_merging_step2")) {
                nodesThatAreDoneWithMergingStep2.add(Integer.parseInt(workerTextMessage.getMessage()));
            }
        }

        System.out.println("Officer knows that Nodes are done with Merging step 2. ");

        //Officer will tell to worker2 en worker3 that they have to do one final merge together. This means they
        //will have to merge 'step2-even-head' and 'step2-odd-tail' together in order to have the left-mid and right-mid
        //elements in the right order

        w2Communicator.send(session.createObjectMessage(new WorkerTextMessage("produce", "step3-head")));
        w2Communicator.send(session.createObjectMessage(new WorkerTextMessage("consume", "step3-tail")));

        w3Communicator.send(session.createObjectMessage(new WorkerTextMessage("produce", "step3-tail")));
        w3Communicator.send(session.createObjectMessage(new WorkerTextMessage("consume", "step3-head")));

        //W1 en w4 zijn klaar, dus deze kunnen verder.
        w1Communicator.send(session.createObjectMessage(new WorkerTextMessage("continue", "true")));
        w4Communicator.send(session.createObjectMessage(new WorkerTextMessage("continue", "true")));


        List<MessageProducer> commmunicators = new ArrayList<>();
        commmunicators.addAll(Arrays.asList(w1Communicator, w2Communicator, w3Communicator, w4Communicator));

        int i = 0;
       Queue mergedAlphaQueue = session.createQueue("merged-Alpha");
       MessageConsumer alphaConsumer =factory.createConsumerQueue(session, "merged-Alpha");
       ArrayList<Node> coinListsInOrderSorted = new ArrayList<>();
        while (i < commmunicators.size()) {
                commmunicators.get(i).send(session.createObjectMessage(new WorkerTextMessage("send-to-alpha", "well done mate")));
               coinListsInOrderSorted.add(0, ((CoinListMessage)( (ObjectMessage) alphaConsumer.receive()).getObject()).getNode());
               i++;
        }

        System.out.println("ALLE RESULTATEN ZIJN BINNENGEHENGELD DOOR DE OFFICIER");

        System.out.println("-----------------------------------------------------");
        for(Node n : coinListsInOrderSorted){
            System.out.println(coinListsInOrderSorted.indexOf(n)+1 + ": \n");
            n.revealGenealogy();
            System.out.println(new Sort().isSorted(n) ? "Sorted" : "Unsorted");
        }

        String[] mergedQueues = workerCommunication.createMergedQueues();

    }


//
//
//
//        /**
//         * Special case in geval van slechts 2 nodes
//         */
//        if (AMOUNT_OF_WORKERS <= 2) { //Special case waarbij de hele lijst gesorteerd is na 1 keer mergen
//            for (int i = 0; i < AMOUNT_OF_WORKERS; i++) {
//                MessageProducer communicator = startNodeMessageServer(nodesThatAreDoneWithMerging.get(i), factory);
//                communicator.send(session.createObjectMessage(new WorkerTextMessage("queue_name", mergedQueues[0])));
//            }
//            MessageConsumer mergedAlphaConsumer = factory.createConsumerQueue(session, "merged-Alpha");
//            Node comletelySortedNode = sortAlphaQueueAndGet(mergedAlphaConsumer, session);
//
//            System.out.println("The entire merge sort process is finished. This is the end result. ");
//            comletelySortedNode.revealGenealogy();
//
//
//            /**
//             * Meer dan 2 nodes
//             */
//        } else {
//            MessageConsumer mergedAlphaConsumer = factory.createConsumerQueue(session, "merged-Alpha");
//            sortAlphaQueueAndGet(mergedAlphaConsumer, session);
//            /**
//             * Enkel gebruik maken van merged0, merged1 en merged-Alpha.
//             * De gemergede resultaten door sturen naar QueueAlpha.
//             * Methode aanmaken voor dinges vanaf regel 81. Deze merged de Alpha Merged naar 1 gesorteerde node.
//             */
//        }
//
//
//


    public static Node combine(Node first, Node second) {
        if (first.c.compareTo(second.c) > 0) {
            first.setPrev(second);
            return first;
        } else {
            second.setPrev(first);
            return second;
        }
    }

//    private static Node sortAlphaQueueAndGet(MessageConsumer alphaConsumer) throws JMSException {
//        ArrayList<LinkedList> sortedLists = new ArrayList<>();
//        LinkedList sortedList = new LinkedList();
//        ArrayList<Node> allHeadsAndTails = new ArrayList<>();
//        int i = 0;
//        while (i < AMOUNT_OF_WORKERS) {
//            Node receviedNode = ((CoinListMessage) ((ObjectMessage) alphaConsumer.receive()).getObject()).getNode();
//            allHeadsAndTails.add(receviedNode);
//            LinkedList headOrTail = new LinkedList();
//            headOrTail.addTailNode(receviedNode);
//            sortedLists.add(headOrTail);
//
//            i++;
//        }
//        System.out.println("========== MERGED =============");
//
//        if(AMOUNT_OF_WORKERS == 1){
//            return allHeadsAndTails.get(0);
//        }
//        if(AMOUNT_OF_WORKERS == 2){
//            sortedList.mergeSortedHeadsAndTails(allHeadsAndTails.get(0), allHeadsAndTails.get(1));
//        }
//        if(AMOUNT_OF_WORKERS == 3){
//
//            sortedList.mergeSortedHeadsAndTails(allHeadsAndTails.get(2), sortedList.mergeSortedHeadsAndTails(allHeadsAndTails.get(0), allHeadsAndTails.get(1)));
//        }
//        if(AMOUNT_OF_WORKERS == 4){
//            sortedList.mergeSortedHeadsAndTails(sortedList.mergeSortedHeadsAndTails(allHeadsAndTails.get(0), allHeadsAndTails.get(1)), sortedList.mergeSortedHeadsAndTails(allHeadsAndTails.get(2), allHeadsAndTails.get(3)));
//        }
//
//        sortedList.merge(sortedList.giveHeadElement(), sortedList.giveTailElement()).revealGenealogy();
//        sortedList.size();
//        return sortedList.giveHeadElement();

//        ArrayList<Node> sortedNodes = new ArrayList<>();
//        int amountOfHeadsAndTailsOnAlphaQueue = getQueueSize(session,session.createQueue("merged-Alpha"));
//        while (sortedNodes.size() < amountOfHeadsAndTailsOnAlphaQueue) {
//            sortedNodes.add(((CoinListMessage) ((ObjectMessage) alphaConsumer.receive()).getObject()).getNode());
//        }
//
//        Node completelySortedNode;
//
//        LinkedList completelySortedCoins = new LinkedList();
//
//        if (sortedNodes.size() == 1) {
//            System.out.println("=========================");
//            completelySortedNode = completelySortedCoins.merge(sortedNodes.get(0), sortedNodes.get(1));
//        } else {
//            completelySortedNode = sortedNodes.get(0);
//
//
//
//        }
//        return completelySortedNode;


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
