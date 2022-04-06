import javax.jms.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Officer implements JMSConnection{
    private static List<Integer> priorityQueue;
    private static Map<Integer, Coin> mids = new HashMap<>();
    private static Map<Integer, Coin>  highs= new HashMap<>();
    private static Map<Integer, Coin> lows = new HashMap<>();


    public static void main(String args[]) throws JMSException {
        System.out.println("Officer is spawned!");

        JMSFactory factory = new JMSFactory();

        Connection connection = factory.startConnection(201, JMSConnection.CONNECTION_URL);
        Session session = factory.createSession(connection);
        MessageConsumer consumer = factory.createConsumerQueue(201, session, "hilomid");

        ConsumerMessageListener hilomidMessageListener = new ConsumerMessageListener();

        consumer.setMessageListener(hilomidMessageListener);


        while (!hilomidMessageListener.allMessagesAreReceived) {
            //Wait till all Nodes/Clients have sent their hi,lo,and mid to the Officer, AND the officer received them.
            System.out.println("Officer received messages!");
            System.out.println("Total messages received by officer: " + hilomidMessageListener.getAllMessages().size());
        }

        System.out.println("BROKE OUT OF OF THE WHILE LOOP! ALL MESSAGES ARE RECEIVED");

        //Officer inspecteert de ontvangen hilomids.
        List<Message> messages = hilomidMessageListener.getAllMessages();
        for (Message lomidhi : messages) { //bevat X aantal wrapped Map's in het geval van X Nodes.
            ObjectMessage lomidhiObj = (ObjectMessage) lomidhi;
            Map<Integer, Map<String, Coin>> map = (HashMap)lomidhiObj.getObject(); //Bevat 3 Coin Objecten
            map.forEach((k, v) ->  map.get(k).forEach((k2, v2) -> addToCorrespondingMap(k, k2, v2))); //HI LO en MID
        }
        System.out.println("tijger");


        Map<Integer, Coin> lowsSorted = new HashMap<>();
        Map<Integer, Coin> highsSorted = new HashMap<>();
        Map<Integer, Coin> midsSorted = new HashMap<>();

        //Officer vergelijkt de ontvangen berichten om te achterhalen wat de head en tail zijn.


        System.out.println("Lows");
        Map<Integer, Coin> sortedLows = sortCoinMap(lows);
        sortedLows.forEach((k, v) -> System.out.println(k + " " + v));

        System.out.println("Mids");
        Map<Integer, Coin> sortedMids = sortCoinMap (mids);
        sortedMids.forEach((k, v) -> System.out.println(k + " " + v));

        System.out.println("Highs");
        Map<Integer, Coin> sortedHighs = sortCoinMap(highs);
        sortedHighs.forEach((k, v) -> System.out.println(k + " " + v));

        Coin lowest = (Coin) sortedLows.values().toArray()[0];
        System.out.println("Found in Node#" +  getKey(sortedLows, lowest));
        System.out.println("Node Lowest rated coin (Head) :  " + lowest.toString());

        Coin highest = (Coin) sortedHighs.values().toArray()[AMOUNT_OF_NODES - 1];
        System.out.println("Found Heighest in Node#" + getKey(sortedHighs, highest));
        System.out.println("Highest rated coin (Tail) : " + highest.toString());
    }



    private static Map<Integer, Coin> sortCoinMap(Map<Integer, Coin> unsortedMap){
        Comparator<Coin> compareByPriceAndMore = Comparator.naturalOrder();
        return unsortedMap.entrySet().stream().sorted(Map.Entry.comparingByValue(compareByPriceAndMore)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    private static <Integer,Coin> Integer getKey(Map<Integer, Coin> map, Coin value){
        return map.entrySet().stream().filter(entry -> value.equals(entry.getValue())).findFirst().map(Map.Entry::getKey).orElse(null);
    }

    private static void addToCorrespondingMap(int nodeId,String range, Coin coin){
        if(range.equals("lo")){
            lows.put(nodeId, coin);
        }else if(range.equals("mid")){
            mids.put(nodeId, coin);
        }else if(range.equals("hi")){
            highs.put(nodeId, coin);
        }
    }

    public Officer(String topic){
        //topic.publish(top;ic)

    }

    public void generateMergerQueues(){

        //Hierin geeft de officer aan welke node head/tail zijn.
        //Dit gebeurt a.d.h.v. hi en lo var.
    }

    public void retrieveHiLoAndPivotsFromNodes(){

    }

    public void retrieveLows(){

    }
    public void retrieveHighs(){

    }
    public void retrievePivots(){

    }


    private void assignMergeTask(int nodeId1, int nodeId2){

    }

    public int compare(){
        return -1;
    }
}
