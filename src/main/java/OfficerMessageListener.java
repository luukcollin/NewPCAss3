//import javax.jms.Message;
//import javax.jms.MessageListener;
//import java.util.ArrayList;
//import java.util.List;
//
//public class OfficerMessageListener implements MessageListener, JMSConnection{
//    List<Message> messages = new ArrayList<>();
//    public boolean allMessagesAreReceived = false;
//
//    @Override
//    public void onMessage(Message message){
//        this.messages.add(message);
//        System.out.println("hello, I see a message has arrived. \nMessage: " + message.toString());
//        System.out.println("I currently retrieved: " + this.getAllMessages().size());
//        allMessagesAreReceived = this.getAllMessages().size() == AMOUNT_OF_NODES;
//    }
//
//}
//