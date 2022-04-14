import javax.jms.*;
import java.util.ArrayList;
import java.util.List;

public class ConsumerMessageListener implements MessageListener, JMSConnection{
    List<Message> messages = new ArrayList<>();
    int[] workersThatAreDoneWithSorting = new int[AMOUNT_OF_WORKERS];
    int amountOfWorkersThatAreDoneSorting = 0;
    private boolean allWorkersHaveTheirOwnListSorted;

    public ConsumerMessageListener(){
        allWorkersHaveTheirOwnListSorted= false;
    }

    @Override
    public void onMessage(Message message){
        this.messages.add(message);
        WorkerTextMessage textMessage = null;
        try {
            textMessage = (WorkerTextMessage) ((ObjectMessage)message).getObject();
        } catch (JMSException e) {
            e.printStackTrace();
        };
        if(textMessage != null){
            if(textMessage.getMessageType().equals("done_sorting")){
                workersThatAreDoneWithSorting[amountOfWorkersThatAreDoneSorting] =Integer.parseInt(String.valueOf(textMessage.getMessage().charAt(textMessage.getMessage().length()-1)));
                amountOfWorkersThatAreDoneSorting++;
            }
        }
        for(int i = 0; i< amountOfWorkersThatAreDoneSorting; i++){
            System.out.println(workersThatAreDoneWithSorting[i]);
        }

        if(amountOfWorkersThatAreDoneSorting == AMOUNT_OF_WORKERS){
            System.out.println("All workers are done with sorting. ");
            allWorkersHaveTheirOwnListSorted = true;
        }

        System.out.println("hello, I see a message has arrived. \nMessage: " + message.toString());
        System.out.println("Topic has currently retrieved: " + this.getAllMessages().size() + " messages.");

    }

    public boolean allWorkersHaveTheirOwnListSorted(){
        return allWorkersHaveTheirOwnListSorted;
    }

    public List<Message> getAllMessages() {
        return this.messages;
    }

}
