import javax.jms.*;
import java.util.List;
import java.util.Map;

public class offe {
    public static void main(String args[]) throws JMSException {
        System.out.println("Officer is spawned!");

        JMSFactory factory = new JMSFactory();

        Connection connection = factory.startConnection(201, JMSConnection.CONNECTION_URL);
        Session session = factory.createSession(connection);
        MessageConsumer consumer = factory.createConsumerQueue(201, session, "hilomid");

        ConsumerMessageListener hilomidMessageListener = new ConsumerMessageListener();

        consumer.setMessageListener(hilomidMessageListener);


        while(!hilomidMessageListener.allMessagesAreReceived) {
            //Wait till all Nodes/Clients have sent their hi,lo,and mid to the Officer, AND the officer received them.
            System.out.println("Officer received messages!");
            System.out.println("Total messages received by officer: " + hilomidMessageListener.getAllMessages().size());
        }

        System.out.println("BROKE OUT OF OF THE WHILE LOOP! ALL MESSAGES ARE RECEIVED");
        List<Message> messages = hilomidMessageListener.getAllMessages();

        for(Message m : messages){
            ObjectMessage karel = (ObjectMessage) m;
            System.out.println(karel.getObject().toString());
        }


//
//        ObjectMessage receivedMessage = (ObjectMessage) consumer.receive();
//        Map<Integer, Map<String, Coin>> m =(Map) receivedMessage.getObject();
//        m.forEach((k, v) -> System.out.println(m.get(k)));
    }
}
