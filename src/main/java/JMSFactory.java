import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class JMSFactory {


    public Connection startConnection(String url) throws JMSException {

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        ((ActiveMQConnectionFactory) connectionFactory).setTrustAllPackages(true);
        Connection connection = connectionFactory.createConnection();
        connection.start();
        return connection;
    }

    public Session createSession(Connection connection) throws JMSException {
        return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    public MessageProducer createProducerQueue(Session session, String queueName) throws JMSException {
        Destination destination = session.createQueue(queueName);
        return session.createProducer(destination);
    }

    public MessageConsumer createConsumerQueue(Session session, String queueName) throws JMSException {
        Destination destination = session.createQueue(queueName);
        return session.createConsumer(destination);

    }

}
