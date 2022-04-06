import javax.jms.*;
import java.util.Map;

public class offe {
    public static void main(String args[]) throws JMSException {
        System.out.println("Officer is spawned!");

        JMSFactory factory = new JMSFactory();

        Connection connection = factory.startConnection(201, JMSConnection.CONNECTION_URL);
        Session session = factory.createSession(connection);
        MessageConsumer consumer = factory.createConsumerQueue(201, session, "hilomid");
        ObjectMessage receivedMessage = (ObjectMessage) consumer.receive();
        Map<Integer, Map<String, Coin>> m =(Map) receivedMessage.getObject();

        m.forEach((k, v) -> System.out.println(m.get(k)));
    }
}
