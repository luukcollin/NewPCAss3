import javax.jms.*;
import java.io.Serializable;
import java.util.*;


public class Worker implements JMSConnection{


    public static void main(String[] args) throws JMSException {

        int workerId= 0;
        int numClients = 4;
        // process command line arguments, decide to assume master role or worker role
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--nodeId")) {
                i++;
                workerId = Integer.parseInt(args[i]);
            } else if (args[i].equals("--numClients")) {
                i++;
                numClients = Integer.valueOf(args[i]);
            }
        }




        //Set workerid
        List<Coin>  unsortedElements = generateElements();

            System.out.println("Sorting the elements will start");
            Timer.start();
        List<Coin> sortedElements =  sortUnsortedElements(unsortedElements);
            Timer.stop();
            System.out.println("Worker(" + workerId + ")" + ": Sorting the elements is done");

        System.out.println(String.format("Worker {0} sends his hashmap to queue. ", workerId));
        //Send to lomihi queue.

        JMSFactory connectionsFactory = new JMSFactory();
        Connection connection =connectionsFactory.startConnection(workerId, CONNECTION_URL);
        //Session session = startSession(clientId, URL);
        Session session = connectionsFactory.createSession(connection);
        MessageProducer producer = connectionsFactory.createProducerQueue(workerId, session, "hilomid");
//        ObjectMessage message = (ObjectMessage)createMapMessage(workerId, sortedElements, session);


        producer.send(createMapMessage(workerId, sortedElements, session));
        System.out.println("Hashmap message should be sent. ");

        //Worker subscribet op het Merger Topic wanneer deze zijn ongesorteerde lijst heeft gesorteerd.
            //TODO topic.subscribe(topic)
            //MessageListener op topic zetten.
            //OnMessage(){ this.stage = Integer.parseInt(message);

            //Stuur lo, mid, hi naar officer queue.
            //TODO Worker maakt instances van Mergers. Deze zijn opgestart met javabin ding. Deze krijgen de sortedElements mee.
            //TODO op dit moment weet Officer al lo, mid, hi van alle nodes.

        //Wacht voor berichten van Officer die gaan over mergen en sorten.

        MessageConsumer consumer = connectionsFactory.createConsumerQueue(8, session, "Hello2");

       ObjectMessage receivedMessage = (ObjectMessage) consumer.receive();
        System.out.println("Message is received: ");
        Map<Integer, Map<String, Coin>> m =(Map) receivedMessage.getObject();
        System.out.println(m);
//        System.out.println(receivedMessage.getObject());
        System.out.println("LOL: ");
        System.out.println(m.get(12).get("lo"));
        final long[] i = {0};
        m.forEach((k, v) -> System.out.println("K: " + k + ", V: " + v));
        connection.close();
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
