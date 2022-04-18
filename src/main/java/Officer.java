

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.sql.SQLOutput;
import java.util.*;
import java.util.stream.Collectors;

public class Officer implements JMSConnection{
    private static List<Integer> priorityQueue;
    private static Map<Integer, Coin> mids = new HashMap<>();
    private static Map<Integer, Coin>  highs= new HashMap<>();
    private static Map<Integer, Coin> lows = new HashMap<>();


    public static void main(String args[]) throws JMSException {
        System.out.println("Officer is spawned!");

        JMSFactory factory = new JMSFactory();

        Connection connection = factory.startConnection( CONNECTION_URL);
        Session session = factory.createSession(connection);
        Topic officerTopic = factory.createTopic(session, "officerTopic");

        /*
        //Maak een topic aan waar Workers berichten naar kunnen toe sturen

        ActiveMQConnectionFactory topicConnection = new ActiveMQConnectionFactory(CONNECTION_URL);
        TopicSession officerSession = topicConnection.createTopicConnection().createTopicSession(true, 0);
        TopicSubscriber subscriber = officerSession.createSubscriber(officerTopic);
*/



        System.out.println("All workers are done with sorting their own list.");

        WorkerCommunication communications = new WorkerCommunication(AMOUNT_OF_WORKERS);
        for(String queue : communications.createMergedQueues()){

        }



            //TODO: Zet de HIGH op de HEAD
            //TODO: Zet de LOW op de Tail
            //TODO: haal de HIGH uit de sortedMap, evenals de LOW




        MessageConsumer consumer = factory.createConsumerQueue(session, "officer");




        List<Integer> nodesThatAreDoneWithSorting = new ArrayList<>();
        int amountOfNodesThatAreDoneWithSorting = 0;

        ArrayList<Integer> nodesThatAreDoneWithMerging = new ArrayList<Integer>();
        while(nodesThatAreDoneWithMerging.size() < AMOUNT_OF_WORKERS){
            System.out.println("wachten op nodes die klaar zijn met mergen");
            WorkerTextMessage workerTextMessage = (WorkerTextMessage) ((ObjectMessage) consumer.receive()).getObject();
            if(workerTextMessage.getMessageType().equals("done_merging")){
                nodesThatAreDoneWithMerging.add(Integer.parseInt(workerTextMessage.getMessage()));
            }
        }
        System.out.println("Officer konws everyone is done with merging.");
        WorkerCommunication workerCommunication = new WorkerCommunication(AMOUNT_OF_WORKERS);
        String[] mergedQueues = workerCommunication.createMergedQueues();
        if(AMOUNT_OF_WORKERS <= 2){ //Special case waarbij de hele lijst gesorteerd is na 1 keer mergen
            MessageProducer communicator = startNodeMessageServer(nodesThatAreDoneWithMerging.get(0), factory);
            communicator.send(session.createObjectMessage(new WorkerTextMessage("queue_name", mergedQueues[0])));
        }


//            //Check of er meer dan 1 worker klaar is met sorteren
//            if(nodesThatAreDoneWithSorting.size() > 1){
//                //Geef een queue aan de workers waar ze op kunnen mergen
//                sendMergeMessage(messageProducers[nodesThatAreDoneWithSorting.get(0)-1]
//                        , session, mergerQueueIndex, "head");
//
//                sendMergeMessage( messageProducers[nodesThatAreDoneWithSorting.get(1)-1]
//                        , session, mergerQueueIndex, "tail");
//                //verhoog de mergerqueueindex
//                mergerQueueIndex++;
//                //Verwijder de workers die klaar zijn uit de lijst, omdat ze nu weer een taak hebben
//                nodesThatAreDoneWithSorting.remove(nodesThatAreDoneWithSorting.get(0));
//                nodesThatAreDoneWithSorting.remove(nodesThatAreDoneWithSorting.get(0));
//
//                System.out.println("Just assigned merger task too workers");
//            }







        //   Session session = factory.createSession(connection);
//        MessageConsumer consumer = factory.createConsumerQueue(session, "hilomid");
//
//        ConsumerMessageListener hilomidMessageListener = new ConsumerMessageListener();
//
//        consumer.setMessageListener(hilomidMessageListener);
//
//
//        while (!hilomidMessageListener.allMessagesAreReceived) {
//            //Wait till all Nodes/Clients have sent their hi,lo,and mid to the Officer, AND the officer received them.
//            System.out.println("Officer received messages!");
//            System.out.println("Total messages received by officer: " + hilomidMessageListener.getAllMessages().size());
//        }
//
//        System.out.println("BROKE OUT OF OF THE WHILE LOOP! ALL MESSAGES ARE RECEIVED");
//
//        //Officer inspecteert de ontvangen hilomids.
//        List<Message> messages = hilomidMessageListener.getAllMessages();
//        for (Message lomidhi : messages) { //bevat X aantal wrapped Map's in het geval van X Nodes.
//            ObjectMessage lomidhiObj = (ObjectMessage) lomidhi;
//            Map<Integer, Map<String, Coin>> map = (HashMap)lomidhiObj.getObject(); //Bevat 3 Coin Objecten
//            map.forEach((k, v) ->  map.get(k).forEach((k2, v2) -> addToCorrespondingMap(k, k2, v2))); //HI LO en MID
//        }
//        System.out.println("tijger");
//
//
//        //Officer vergelijkt de ontvangen berichten om te achterhalen wat de head en tail zijn.
//
//
//        System.out.println("Lows");
//        Map<Integer, Coin> sortedLows = sortCoinMap(lows);
//        sortedLows.forEach((k, v) -> System.out.println(k + " " + v));
//
//        System.out.println("Mids");
//        Map<Integer, Coin> sortedMids = sortCoinMap (mids);
//        sortedMids.forEach((k, v) -> System.out.println(k + " " + v));
//
//        System.out.println("Highs");
//        Map<Integer, Coin> sortedHighs = sortCoinMap(highs);
//        sortedHighs.forEach((k, v) -> System.out.println(k + " " + v));
//
//        Coin lowest = (Coin) sortedLows.values().toArray()[0];
//        System.out.println("Found LOWEST in Node#" +  getKey(sortedLows, lowest));
//        System.out.println("Node Lowest rated coin (Head) :  " + lowest.toString());
//
//        Coin highest = (Coin) sortedHighs.values().toArray()[AMOUNT_OF_NODES - 1];
//        System.out.println("Found HIGHEST in Node#" + getKey(sortedHighs, highest));
//        System.out.println("Highest rated coin (Tail) : " + highest.toString());
//
//
//        getKey(sortedHighs, highest);
        //Removing highest element from this nodes list.



    }





    private static MessageProducer startNodeMessageServer(Integer originNodeId, JMSFactory factory) throws JMSException{
        String nodeMessageServer = "message-server-" + originNodeId;
        Connection connection = factory.startConnection( CONNECTION_URL);
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



    public Officer(String topic){


    }

    public void generateMergerQueues(){
        //Hierin geeft de officer aan welke node head/tail zijn.
    }

    public void retrieveHiLoAndPivotsFromNodes(){

    }

    private void assignMergeTask(int nodeId1, int nodeId2){

    }


}
