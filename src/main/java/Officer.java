import javax.jms.*;
import java.util.*;

public class Officer implements JMSConnection {

    public static void main(String args[]) throws JMSException {
        int numClients = Integer.parseInt(args[1]);
        System.out.println("NUMBER CLIENTS: " + numClients);
        JMSFactory factory = new JMSFactory();
        Connection connection = factory.startConnection(CONNECTION_URL);
        Session session = factory.createSession(connection);
        MessageConsumer consumer = factory.createConsumerQueue(session, "officer");


        ArrayList<Integer> nodesThatAreDoneWithMerging = new ArrayList<Integer>();
        //In geval van oneven aantal nodes is er in deze stap 1 merge minder t.o.z. van even aantal nodes
        int numClientsExpectedToBeDoneWithMerging = numClients % 2 == 0 ? numClients : numClients - 1;
        while (nodesThatAreDoneWithMerging.size() < numClientsExpectedToBeDoneWithMerging) {
            System.out.println("wachten op nodes die klaar zijn met mergen");
            WorkerTextMessage workerTextMessage = (WorkerTextMessage) ((ObjectMessage) consumer.receive()).getObject();
            if (workerTextMessage.getMessageType().equals("done_merging")) {
                nodesThatAreDoneWithMerging.add(Integer.parseInt(workerTextMessage.getMessage()));
            }
        }
        System.out.println("Officer konws everyone is done with merging.");

        //Create message servers for Officer to communicate with workers
        WorkerCommunication workerCommunication = new WorkerCommunication(numClients);
        String[] messageServers = workerCommunication.createMessageServerQueueNames();
        List<MessageProducer> commmunicators = new ArrayList<>();
        for (int i = 0; i < messageServers.length; i++) {
            commmunicators.add(factory.createProducerQueue(session, messageServers[i]));
        }

        /**
         * In geval van 4 Nodes:
         * 1. Merge 1 and 2, merge 3 and 4. [1: LEFT, 2: RIGHT, 3: LEFT, 4: RIGHT]
         * 2. Merge 1 and 3, merge 2 and 4. [1: LEFT, 2: LEFT, 3: RIGHT, 4: RIGHT]
         * 3. Merge 2 and 3. [1: LEFT, 2 left MID, 3 right MID, 4 RIGHT]
         */

        /**
         * In geval van 3 Nodes:
         * 1. Merge 1 and 2.
         * 2. Merge Merged-1-2 with 3.
         */


        //In geval van 4 nodes of meer.
        if (numClients >= 4) {
            String[] odds = new String[]{"head-a", "tail-a"};
            String[] evens = new String[]{"head-x", "tail-x"};
            boolean produce = true;
            boolean even = true;
            for (MessageProducer communicator : commmunicators) {
                int index = 0;
                if (numClients >= 3) {
                    even = commmunicators.indexOf(communicator) % 2 == 0;
                }
                System.out.println("lols " + even);
                communicator.send(session.createObjectMessage(new WorkerTextMessage(produce ? "produce" : "consume", even ? evens[index++] : odds[index++])));
                communicator.send(session.createObjectMessage(new WorkerTextMessage(produce ? "consume" : "produce", even ? evens[index] : odds[index])));
                if (even) {
                    produce = !produce;
                }
            }
            //Officer will wait again till Nodes are done with merging...
            ArrayList<Integer> nodesThatAreDoneWithMergingStep2 = new ArrayList<Integer>();
            while (nodesThatAreDoneWithMergingStep2.size() < numClients) {
                System.out.println("wachten op nodes die klaar zijn met mergen step 2");
                WorkerTextMessage workerTextMessage = (WorkerTextMessage) ((ObjectMessage) consumer.receive()).getObject();
                if (workerTextMessage.getMessageType().equals("done_merging_step2")) {
                    nodesThatAreDoneWithMergingStep2.add(Integer.parseInt(workerTextMessage.getMessage()));
                }
            }
            System.out.println("Officer knows that Nodes are done with Merging step 2. ");

            //Officer will tell to worker2 en worker3 that they have to do one final merge together. This means they
            //will have to merge 'step2-even-head' and 'step2-odd-tail' together in order to have the left-mid and right-mid
            //elements in the right order
            commmunicators.get(1).send(session.createObjectMessage(new WorkerTextMessage("produce", "step3-head")));
            commmunicators.get(1).send(session.createObjectMessage(new WorkerTextMessage("consume", "step3-tail")));
            commmunicators.get(2).send(session.createObjectMessage(new WorkerTextMessage("produce", "step3-tail")));
            commmunicators.get(2).send(session.createObjectMessage(new WorkerTextMessage("consume", "step3-head")));

            //W1 en w4 zijn al klaar, dus deze kunnen verder.
            commmunicators.get(0).send(session.createObjectMessage(new WorkerTextMessage("continue", "true")));
            commmunicators.get(3).send(session.createObjectMessage(new WorkerTextMessage("continue", "true")));
        } else {
            for (MessageProducer communicator : commmunicators) {
                communicator.send(session.createObjectMessage(new WorkerTextMessage("continue", "true")));
            }
        }
        int i = 0;
        MessageConsumer alphaConsumer = factory.createConsumerQueue(session, "merged-Alpha");
        ArrayList<Node> coinListsInOrderSorted = new ArrayList<>();
        while (i < commmunicators.size()) {
            commmunicators.get(i).send(session.createObjectMessage(new WorkerTextMessage("send-to-alpha", "well done mate, thanks for you work")));
            coinListsInOrderSorted.add(0, ((CoinListMessage) ((ObjectMessage) alphaConsumer.receive()).getObject()).getNode());
            i++;
        }

        System.out.println("-----------------------------------------------------");

        LinkedList my = new LinkedList();
        if (numClients % 2 != 0) {
            Node firstAndSecond = my.merge(coinListsInOrderSorted.get(0), coinListsInOrderSorted.get(1));
            coinListsInOrderSorted.set(0, my.merge(firstAndSecond, coinListsInOrderSorted.get(2)));
            coinListsInOrderSorted.get(0).revealGenealogy();
            System.out.println("Sorted: " + new Sort().isSorted(coinListsInOrderSorted.get(0)));
            System.out.println("AMOUNT OF ELEMENTS: " + (coinListsInOrderSorted.get(0).revealGenealogy().size()));
        } else {
            for (Node n : coinListsInOrderSorted) {
                System.out.println(coinListsInOrderSorted.indexOf(n) + 1 + ": \n");
                n.revealGenealogy();
                System.out.println(new Sort().isSorted(n) ? "Sorted" : "Unsorted");
            }
        }
        System.out.println("ALLE RESULTATEN ZIJN BINNENGEHENGELD DOOR DE OFFICIER");
    }
}
