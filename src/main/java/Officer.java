import javax.jms.*;
import java.util.*;

/**
 * In geval van 4 Nodes:
 * 1. Merge 1 and 2, merge 3 and 4. [1: LEFT, 2: RIGHT, 3: LEFT, 4: RIGHT]
 * 2. Merge 1 and 3, merge 2 and 4. [1: LEFT, 2: LEFT, 3: RIGHT, 4: RIGHT]
 * 3. Merge 2 and 3. [1: LEFT, 2 left MID, 3 right MID, 4 RIGHT]
 * <p>
 * In geval van 3 Nodes:
 * 1. Merge 1 and 2.
 * 2. Merge Merged-1-2 with 3.
 * <p>
 * In geval van 2 Nodes:
 * 1. Merge 1 and 2.
 * <p>
 * In geval van 1 Node:
 * 1. print direct de gesorteerde lijst
 */
public class Officer implements JMSConnection {

    public static void main(String[] args) throws JMSException {
        int numClients = Integer.parseInt(args[1]);
        JMSFactory factory = new JMSFactory();
        Connection connection = factory.startConnection(CONNECTION_URL);
        Session session = factory.createSession(connection);
        MessageConsumer consumer = factory.createConsumerQueue(session, "officer"); //Known bug: Soms komen er 2 consumers op
        //de officer queue. In principe slaat dit nergens op, maar wordt verholpen door de pc opnieuw op te starten. Enkel activemq opnieuw opstarten is
        //en alle queues en topics verwijderen is soms niet voldoende. Dit fenomeen staat bekend als een ghost consumer, ofwel dead consumer.


        ArrayList<Integer> nodesThatAreDoneWithMerging = new ArrayList<>();
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
        for (String messageServer : messageServers) {
            commmunicators.add(factory.createProducerQueue(session, messageServer));
        }
        //In geval van 4 nodes of meer.
        if (numClients >= 4) {
            String[] odds = new String[]{"head-a", "tail-a"};
            String[] evens = new String[]{"head-b", "tail-b"};
            boolean produce = true;

            for (MessageProducer communicator : commmunicators) {
                int index = 0;

                boolean even = commmunicators.indexOf(communicator) % 2 == 0;
                communicator.send(session.createObjectMessage(new WorkerTextMessage(produce ? "produce" : "consume", even ? evens[index++] : odds[index++])));
                communicator.send(session.createObjectMessage(new WorkerTextMessage(produce ? "consume" : "produce", even ? evens[index] : odds[index])));
                if (even) {
                    produce = !produce;
                }
            }
            //Officer will wait again till Nodes are done with merging...
            ArrayList<Integer> nodesThatAreDoneWithMergingStep2 = new ArrayList<>();
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
        } else if (numClients == 1) {
            System.exit(0);
        } else {
            for (MessageProducer communicator : commmunicators) {
                communicator.send(session.createObjectMessage(new WorkerTextMessage("continue", "true")));
            }
        }
        int i = 0;
        MessageConsumer alphaConsumer = factory.createConsumerQueue(session, "merged-Alpha");
        List<Node> coinListsInOrderSorted = new ArrayList<>();
        while (i < commmunicators.size()) {
            commmunicators.get(i).send(session.createObjectMessage(new WorkerTextMessage("send-to-alpha", "well done mate, thanks for you work")));
            coinListsInOrderSorted.add(0, ((CoinListMessage) ((ObjectMessage) alphaConsumer.receive()).getObject()).getNode());
            i++;
        }

        LinkedList linkedlist = new LinkedList();
        List<Coin> sortedResult = new ArrayList<>();
        if (numClients % 2 != 0) {
            Node firstAndSecond = linkedlist.merge(coinListsInOrderSorted.get(0), coinListsInOrderSorted.get(1));
            coinListsInOrderSorted.set(0, linkedlist.merge(firstAndSecond, coinListsInOrderSorted.get(2)));
            sortedResult = coinListsInOrderSorted.get(0).revealGenealogy();
        } else {
            coinListsInOrderSorted = new Sort().sortNodesEasily(coinListsInOrderSorted); //In principe komen alle nodes al in volgorde aan, maar er zijn
            //wel eens specifieke gevallen dat dat niet zo is, daarom sorteerd we met een simpele bubbelsort de lijsten voor de zekerheid, dit gaat bij 4 nodes om 4 elementen.
            Node result = null;
            for (i = 0; i < coinListsInOrderSorted.size() - 1; ) {
                if (result == null) {
                    result = linkedlist.merge(coinListsInOrderSorted.get(i++), coinListsInOrderSorted.get(i++));
                } else {
                    result = linkedlist.merge(result, linkedlist.merge(coinListsInOrderSorted.get(i++), coinListsInOrderSorted.get(i++)));
                }
                sortedResult = result.revealGenealogy();
            }
            for (Coin c : sortedResult) {
                System.out.println(c.toString());
            }
        }
        System.out.println("^^^ The sorted list is printed above. (Sorted on price, then marketcap, etc.) ^^^ \n");



        System.out.println("===== QUICK TEST RESULTS =====");
        ListTest listTester = new ListTest();
        System.out.println( format(listTester.isSorted(sortedResult)) + "Elementen in sortedResult zijn gesorteerd: ");
        System.out.println(format(listTester.containsOnlyUniqueElements(sortedResult)) + "Elementen in sortedResult zijn allemaal uniek: ");
        System.out.println(format(listTester.listHasExpectedSize(sortedResult, numClients, AMOUNT_OF_ELEMENTS)) + "Het aantal elementen in de lijst is hetzelfde als verwacht: ");
        System.out.println("\n \u001B[40m \u001B[46m Totaal van: " + sortedResult.size() + " elementen  \u001B[0m" );
        System.exit(0);


    }

    private static String format(boolean passed){
        String text = passed? "PASSED": "FAILED";
        String clear = "\u001B[0m";
        String test_passed = "\u001B[32m";
        String test_failed = "\u001B[31m";
        String format = passed ? test_passed : test_failed;
        return "[" + format + text + clear + "] ";
    }
}
