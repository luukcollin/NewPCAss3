import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class JMSFactory {


    public  Connection startConnection(String url) throws JMSException {

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        ((ActiveMQConnectionFactory) connectionFactory).setTrustAllPackages(true);
        Connection connection = connectionFactory.createConnection();
        connection.start();
        return connection;
    }

    public  Session createSession(Connection connection) throws JMSException {
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        return session;
    }
//    private static Session startSession(int clientId, String url) throws JMSException {
//        Connection connection = startConnection(clientId, url);
//        return createSession(connection);
//    }

    public  MessageProducer createProducerQueue(Session session, String queueName) throws JMSException {
        Destination destination = session.createQueue(queueName);
        MessageProducer producer = session.createProducer(destination);

        return producer;
    }
    public Topic createTopic(Session session, String topicName) throws JMSException {
         return session.createTopic(topicName);
    }


    public MessageConsumer createConsumerQueue(Session session, String queueName) throws JMSException {
        Destination destination = session.createQueue(queueName);
        MessageConsumer consumer = session.createConsumer(destination);
        return consumer;
    }

}
