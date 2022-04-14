import java.io.Serializable;

public class WorkerCommunication implements Serializable {

        private final int amountOfWorkers;

        public WorkerCommunication(int amountOfWorkers) {
            this.amountOfWorkers = amountOfWorkers;

        }


    /**
     *
     *
     * @return de namen van message-servers queues die gebruikt worden door Workers om ekaar
     * te communiceren. De officer kan ook met Workers communiceren op deze queues.
     */
    public String[] createMessageServerQueueNames(){
            String [] queueNames = new String[amountOfWorkers];
            for(int i = 0; i < queueNames.length; i++){
                queueNames[i] = "message-server-" + i;
            }
            return queueNames;
        }

    /**
     *
     * @return de namen van de queues waar elementen samengevoegd worden door meerdere nodes
     */
    public String[] createMergedQueues(){
            String[] queueNames = new String[amountOfWorkers];
            int amountOfQueuesNeeded = (int)Math.ceil(Math.log(amountOfWorkers) / Math.log(2));
            for(int i = 0; i < amountOfQueuesNeeded; i++){
                queueNames[i] = "merged-" + i;
            }
            return queueNames;
        }




    }



