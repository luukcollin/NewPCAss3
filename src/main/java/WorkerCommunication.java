import java.io.Serializable;

public class WorkerCommunication implements Serializable {

    private final int amountOfWorkers;

    public WorkerCommunication(int amountOfWorkers) {
        this.amountOfWorkers = amountOfWorkers;

    }


    /**
     * @return de namen van message-servers queues die gebruikt worden door Workers om ekaar
     * te communiceren. De officer kan ook met Workers communiceren op deze queues.
     */
    public String[] createMessageServerQueueNames() {
        String[] queueNames = new String[amountOfWorkers];
        for (int i = 1; i <= queueNames.length; i++) {
            queueNames[i - 1] = "message-server-" + i;
        }
        return queueNames;
    }
}



