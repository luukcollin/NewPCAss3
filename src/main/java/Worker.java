import javax.jms.*;
import javax.jms.Queue;
import java.util.*;


public class Worker implements JMSConnection {

    static Coin sendBack = null;

    public static void main(String[] args) throws JMSException {
        int workerId = 0;
        int numClients = 2;
        for (int i = 0; i < args.length; i++) {

            if (args[i].equals("--workerId")) {
                i++;
                workerId = Integer.parseInt(args[i]);
            } else if (args[i].equals("--numClients")) {
                i++;
                numClients = Integer.parseInt(args[i]);
            }

        }
        //HeadQueue name
        String hq = getHeadQueue(workerId);
        //TailQueue name
        String tq = getTailQueue(workerId);


        boolean leftWorker = workerId % 2 == 0; //Workers met een even workerId zijn leftworkers
        //Maak een nieuwe instance van een zelfgeschreven LinkedList class
        LinkedList myCoinlist = new LinkedList();

        //Genereer (random) coinObjecten en voeg ze toe per stuk toe aan een lege LinkedList instance; First-in-first-out
        for (Coin c : generateCoinObjects(workerId)) {
            myCoinlist.addTail(c);
        }
        //Sorteer de lijst d.m.v. een recursief Merge Sort algoritme
        myCoinlist.sortList();




        //Zet de connectie op met JMS
        JMSFactory jmsFactory = new JMSFactory();
        Connection connection = jmsFactory.startConnection(CONNECTION_URL);
        Session session = jmsFactory.createSession(connection);

        //Bericht de officer dat ik klaar ben sorteren, m.a.w. klaar om te mergen met andere gesorteerde lijst.
        Topic officerTopic = jmsFactory.createTopic(session, "officerTopic");
        MessageProducer topicProducer = session.createProducer(officerTopic);
        topicProducer.send(session.createObjectMessage(new WorkerTextMessage("done_sorting", "worker" + workerId)));

        boolean done = false;
        MessageProducer messageProducer;
        MessageConsumer messageConsumer;
        Coin sentCoin;
        if (leftWorker) {
            messageProducer = jmsFactory.createProducerQueue(session,  tq);
            messageConsumer = jmsFactory.createConsumerQueue(session, hq);
        } else {
            messageProducer = jmsFactory.createProducerQueue(session, hq);
            messageConsumer = jmsFactory.createConsumerQueue(session,  tq);
        }
        Coin receivedCoin = null;
        String leftOrRightWorker = leftWorker ? "Left worker " : "Right worker ";
        while (!done) {
            if (leftWorker) {
                Coin objectToSend = myCoinlist.giveTailElementAndRemove();
                sentCoin = objectToSend;
//                System.out.println(leftOrRightWorker + ") Sends: " + objectToSend.toString());
                //Stuur de grootste Coin van mijn eigen gesorteerde lijst weg
                messageProducer.send(session.createObjectMessage(objectToSend));
                //Ontvang van een andere worker zijn laagste Coin

                receivedCoin = (Coin) ((ObjectMessage) messageConsumer.receive()).getObject();
                System.out.println(leftOrRightWorker + ") Received: " + receivedCoin.toString());
            } else {
                Coin objectToSend = myCoinlist.giveHeadElementAndRemove();
                sentCoin = objectToSend;
                //Stuur de laagste Coin van mijn eigen gesorteerde lijst weg
//                System.out.println(leftOrRightWorker + ") Sends: " + objectToSend.toString());
                messageProducer.send(session.createObjectMessage(objectToSend));
                //Ontvang van een andere worker zijn laagste Coin
                receivedCoin = (Coin) ((ObjectMessage) messageConsumer.receive()).getObject();

                System.out.println(leftOrRightWorker + ") Received: " + receivedCoin.toString());
            }
            done = processExchangedValues(sentCoin, receivedCoin, myCoinlist, leftWorker);
            System.out.println(done);
        }
        boolean sentBack = false;
        while (!sentBack)
            if (leftWorker && sendBack != null) {
                messageProducer.send(session.createObjectMessage(sendBack));


                Coin receivedCoinBack = (Coin) ((ObjectMessage) messageConsumer.receive()).getObject();
                System.out.println("Left worker received: (SENT BACK) " + receivedCoinBack.toString());
                myCoinlist.addTail(receivedCoinBack);
                System.out.println("======== ADDED TAIL ======");
                sentBack = true;
            } else if (!leftWorker && sendBack != null) {
                messageProducer.send(session.createObjectMessage(sendBack));

                Coin receivedCoinBack = (Coin) ((ObjectMessage) messageConsumer.receive()).getObject();
                myCoinlist.addHead(receivedCoinBack);
                System.out.println("Right worker received: (SENT BACK) " + receivedCoinBack.toString());
                System.out.println("====== ADDED HEAD =======");
                sentBack = true;
            }
        sentBack = false;
        myCoinlist.printList(leftWorker);
        String workerLeftRight = leftWorker ? ") Left worker: " : ") Right worker: ";
        System.out.println(workerId +  workerLeftRight + "is done with sorting");

        //Nu heeft de leftworker enkel getallen (coins) die kleiner zijn dan alle getallen van de rightworker
        //Tijd om de officer te berichten dat alles goed verlopen is
        MessageProducer officerProducer = jmsFactory.createProducerQueue(session, "officer");
        officerProducer.send(session.createObjectMessage(new WorkerTextMessage("done_merging", String.valueOf(workerId))));
        System.out.println("Worker is done with merging. Will take a pause until officer sends commmands");


        String queueToConsume = null;
        String queueToProduce = null;
        MessageConsumer officerMessageConsumer = jmsFactory.createConsumerQueue(session, "message-server-" + workerId);
        while(queueToConsume == null || queueToProduce == null) {
            WorkerTextMessage receivedMessage = (WorkerTextMessage) ((ObjectMessage) officerMessageConsumer.receive()).getObject();
            if (receivedMessage.getMessageType().equals("consume")) {
                queueToConsume = receivedMessage.getMessage();
            } else if (receivedMessage.getMessageType().equals("produce")) {
                queueToProduce = receivedMessage.getMessage();
            }
        }
        MessageProducer messageProducer1 = jmsFactory.createProducerQueue(session,  queueToProduce);
        MessageConsumer messageConsumer1 = jmsFactory.createConsumerQueue(session, queueToConsume);
        System.out.println("Worker " +  workerId + " has received consumerqueue and producerqueue");
        if(AMOUNT_OF_WORKERS > 2){
            leftWorker = !queueToConsume.contains("tail");



            boolean done1=  false;
            while(!done1) {
                if (leftWorker) {
                    Coin objectToSend = myCoinlist.giveTailElementAndRemove();
                    sentCoin = objectToSend;
//                System.out.println(leftOrRightWorker + ") Sends: " + objectToSend.toString());
                    //Stuur de grootste Coin van mijn eigen gesorteerde lijst weg
                    messageProducer1.send(session.createObjectMessage(objectToSend));
                    //Ontvang van een andere worker zijn laagste Coin

                    receivedCoin = (Coin) ((ObjectMessage) messageConsumer1.receive()).getObject();
                    System.out.println(leftOrRightWorker + ") Received: " + receivedCoin.toString());
                } else {
                    Coin objectToSend = myCoinlist.giveHeadElementAndRemove();
                    sentCoin = objectToSend;
                    //Stuur de laagste Coin van mijn eigen gesorteerde lijst weg
//                System.out.println(leftOrRightWorker + ") Sends: " + objectToSend.toString());
                    messageProducer1.send(session.createObjectMessage(objectToSend));
                    //Ontvang van een andere worker zijn laagste Coin
                    receivedCoin = (Coin) ((ObjectMessage) messageConsumer1.receive()).getObject();

                    System.out.println(leftOrRightWorker + ") Received: " + receivedCoin.toString());
                }
                done1 = processExchangedValues(sentCoin, receivedCoin, myCoinlist, leftWorker);
            }

        }

        while (!sentBack)
            if (leftWorker && sendBack != null) {
                messageProducer1.send(session.createObjectMessage(sendBack));


                Coin receivedCoinBack = (Coin) ((ObjectMessage) messageConsumer1.receive()).getObject();
                System.out.println("Left worker received: (SENT BACK) " + receivedCoinBack.toString());
                myCoinlist.addTail(receivedCoinBack);
                System.out.println("======== ADDED TAIL ======");
                sentBack = true;
            } else if (!leftWorker && sendBack != null) {
                messageProducer1.send(session.createObjectMessage(sendBack));

                Coin receivedCoinBack = (Coin) ((ObjectMessage) messageConsumer1.receive()).getObject();
                myCoinlist.addHead(receivedCoinBack);
                System.out.println("Right worker received: (SENT BACK) " + receivedCoinBack.toString());
                System.out.println("====== ADDED HEAD =======");
                sentBack = true;
            }

        sentBack = false;
        System.out.println("WORKED AS A CHARM ==================================================");
        //Notify the officer that I'm done with merging step 2.
        officerProducer.send(session.createObjectMessage(new WorkerTextMessage("done_merging_step2", String.valueOf(workerId))));

        boolean continueWithProcess = false;
        MessageConsumer step3Consumer = null;
        MessageProducer step3Producer = null;
        boolean skipStep3 = false;
        boolean left =false;
        while(!continueWithProcess){

            if(step3Consumer == null || step3Producer == null) {
                WorkerTextMessage officerMessage = (WorkerTextMessage) ((ObjectMessage) officerMessageConsumer.receive()).getObject();
                if (officerMessage.getMessageType().equals("continue")) {
                    continueWithProcess = officerMessage.getMessage().equals("true");
                    skipStep3 = continueWithProcess;
                } else if (officerMessage.getMessageType().equals("produce")) {
                    String receivedQueueToProduce = officerMessage.getMessage();
                    left = !receivedQueueToProduce.contains("head");
                    step3Producer = jmsFactory.createProducerQueue(session, receivedQueueToProduce);
                } else if (officerMessage.getMessageType().equals("consume")) {
                    String receivedQueueToConsume = officerMessage.getMessage();
                    step3Consumer = jmsFactory.createConsumerQueue(session, receivedQueueToConsume);
                }
            }else {
                boolean done2 = false;
                System.out.println("WORKER " + workerId +  " isLeft: " + left);
                while (!done2) {
                    if (left) {
                        Coin objectToSend = myCoinlist.giveTailElementAndRemove();
                        sentCoin = objectToSend;
//                System.out.println(leftOrRightWorker + ") Sends: " + objectToSend.toString());
                        //Stuur de grootste Coin van mijn eigen gesorteerde lijst weg
                        step3Producer.send(session.createObjectMessage(objectToSend));
                        //Ontvang van een andere worker zijn laagste Coin

                        receivedCoin = (Coin) ((ObjectMessage) step3Consumer.receive()).getObject();
                        System.out.println(leftOrRightWorker + ") Received: " + receivedCoin.toString());
                    } else {
                        Coin objectToSend = myCoinlist.giveHeadElementAndRemove();
                        sentCoin = objectToSend;
                        //Stuur de laagste Coin van mijn eigen gesorteerde lijst weg
//                System.out.println(leftOrRightWorker + ") Sends: " + objectToSend.toString());
                        step3Producer.send(session.createObjectMessage(objectToSend));
                        //Ontvang van een andere worker zijn laagste Coin
                        receivedCoin = (Coin) ((ObjectMessage) step3Consumer.receive()).getObject();

                        System.out.println(leftOrRightWorker + ") Received: " + receivedCoin.toString());
                    }
                    done2 = processExchangedValues(sentCoin, receivedCoin, myCoinlist, left);
                    continueWithProcess = done2;
                }
            }
        }
        while (!sentBack && !skipStep3)
            if (leftWorker && sendBack != null) {
                step3Producer.send(session.createObjectMessage(sendBack));


                Coin receivedCoinBack = (Coin) ((ObjectMessage) step3Consumer.receive()).getObject();
                System.out.println("Left worker received: (SENT BACK) " + receivedCoinBack.toString());
                myCoinlist.addTail(receivedCoinBack);
                System.out.println("======== ADDED TAIL ======");
                sentBack = true;
            } else if (!leftWorker && sendBack != null) {
                step3Producer.send(session.createObjectMessage(sendBack));

                Coin receivedCoinBack = (Coin) ((ObjectMessage) step3Consumer.receive()).getObject();
                myCoinlist.addHead(receivedCoinBack);
                System.out.println("Right worker received: (SENT BACK) " + receivedCoinBack.toString());
                System.out.println("====== ADDED HEAD =======");
                sentBack = true;
            }

        sentBack = false;
        System.out.println("ALLE BERICHTEN VAN STAP 1 TOT EN MET 3 ZIJN GEMERGED. NU ALLES STUK VOOR STUK NAAR ALPHA STUREN");
        //Worker bepaalt naar welke queue hij zijn gemergde lijst moet sturen (HEAD location or TAIL)
//        String queueName = leftWorker ? "head" + determineQueueId(workerId) : "tail" + determineQueueId(workerId);
        String queueName = "merged-Alpha";

        System.out.println("Worker stuurt berichten head/tail naar " + queueName + ", but only after he receives a message from the Officer to do so.");
        boolean doneWithEntireProcess = false;
        while(!doneWithEntireProcess) {
            WorkerTextMessage officerMessage = (WorkerTextMessage) ((ObjectMessage) officerMessageConsumer.receive()).getObject();
            if (officerMessage.getMessageType().equals("send-to-alpha")) {
                //Stuur linkedList naar merged-Alpha queue
                MessageProducer coinListProducer = jmsFactory.createProducerQueue(session, queueName);
                coinListProducer.send(session.createObjectMessage(new CoinListMessage(myCoinlist.giveHeadElement())));
                doneWithEntireProcess = true;
            }
        }
        System.out.println("ghehehhehehehehehehehehehehe i'm done");



    }


    /**
     * @param session
     * @param queue
     * @return het aantal enqueuede berichten op @queue
     */
    private static int getQueueSize(Session session, Queue queue) {
        int count = 0;
        try {
            QueueBrowser browser = session.createBrowser(queue);
            Enumeration elems = browser.getEnumeration();
            while (elems.hasMoreElements()) {
                elems.nextElement();
                count++;
            }
        } catch (JMSException ex) {
            ex.printStackTrace();
        }
        return count;
    }

    /**
     * NODE A; 2 3 5 6 7 12
     * NODE B: 1 4 8 9 10 11
     * <p>
     * <p>
     * 1st round: A[12] --> B[1]   A: 1 2 3 5 6 7    B: 4 8 9 10 11 12
     * 2nd round: A[7] --> B[4]    A: 1 2 3 4 5 6    B: 7 8 9 10 11 12
     * 3rd round: A[6] --> B[8] //Zal niet accepteren omdat
     * <p>
     * expected A: 1 2 3 4 5 6
     * expected B: 7 8 9 10 11 12
     */


    private static boolean processExchangedValues(Coin sentCoin, Coin receivedCoin, LinkedList myList, boolean isLeftWorker) {
        if(isLeftWorker){
            if(sentCoin.compareTo(receivedCoin) < 0){
                sendBack = receivedCoin;
                return true;
            }
            if(receivedCoin.compareTo(myList.giveHeadElement().c) < 0){
                myList.addHead(receivedCoin);
                return false;
            }
            myList.insert(receivedCoin);
        }else{
            if(sentCoin.compareTo(receivedCoin) > 0){
                sendBack = receivedCoin;
                return true;
            }
            if(receivedCoin.compareTo(myList.giveTailElement().c) > 0){
                myList.addTail(receivedCoin);
                return false;
            }
            myList.insert(receivedCoin);
        }
        return false;

    }

    public static List<Coin> generateCoinObjects(int workerId) {
        return new DataGenerator(AMOUNT_OF_ELEMENTS).generate(workerId);
    }

    public boolean isWorkerTextMessage(ObjectMessage objectMessage) {
        return objectMessage instanceof WorkerTextMessage;
    }

    private static int determineQueueId(int workerId){
        if(workerId <= 2){
            return 0;
        }else{
            return 1;
        }

    }
    private static String getHeadQueue(int workerId){
        return "head"+determineQueueId(workerId);
    }
    private static String getTailQueue(int workerId){
        return "tail"+determineQueueId(workerId);
    }


}
