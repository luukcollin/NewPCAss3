import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import java.util.List;

public class JMSMerger implements JMSConnection{

    public static void main(String[] args) throws JMSException {



        JMSFactory factory = new JMSFactory();
        Connection connection = factory.startConnection(CONNECTION_URL);
        Session session = factory.createSession(connection);
        MessageConsumer consumer;
        int mergedId;
        int numClients;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--nodeId")) {
                i++;
                mergedId = Integer.parseInt(args[i]);
                if(mergedId % 2== 0){
                    consumer = factory.createConsumerQueue(session, (""+mergedId));
                }else{
                    consumer = factory.createConsumerQueue(session, (""+mergedId));
                }
                 } else if (args[i].equals("--numClients")) {
                i++;
                numClients = Integer.valueOf(args[i]);
            }
        }
    }
}
