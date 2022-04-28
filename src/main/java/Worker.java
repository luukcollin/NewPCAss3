import javax.jms.*;
import java.util.*;


public class Worker implements JMSConnection {

    static Coin sendBack = null;
    //Maak een nieuwe instance van een zelfgeschreven LinkedList class
    static LinkedList myCoinList = new LinkedList();

    public static void main(String[] args) throws JMSException {
        int workerId = 0;
        int numClients = Integer.parseInt(args[3]);
        ;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--workerId")) {
                i++;
                workerId = Integer.parseInt(args[i]);
            }
        }


        //This a special case, where the node is the third wheel.
        boolean specialWorker = (numClients % 2 == 1 && workerId == numClients);
        boolean leftWorker = workerId % 2 == 0; //Workers met een even workerId zijn leftworkers

        //Genereer (random) coinObjecten en voeg ze toe per stuk toe aan een lege LinkedList instance; First-in-first-out
        for (Coin c : generateCoinObjects(workerId)) {
            myCoinList.addTail(c);
        }
        //Sorteer de lijst d.m.v. een recursief Merge Sort algoritme
        myCoinList.sortList();
        if (numClients == 1) {
            myCoinList.printList(true);
        } else {

            //Zet de connectie op met JMS
            JMSFactory jmsFactory = new JMSFactory();
            Connection connection = jmsFactory.startConnection(CONNECTION_URL);
            Session session = jmsFactory.createSession(connection);

            //HeadQueue name
            String hq = getHeadQueue(workerId);
            //TailQueue name
            String tq = getTailQueue(workerId);
            if (!specialWorker) {
                MessageProducer messageProducer;
                MessageConsumer messageConsumer;
                if (leftWorker) {
                    messageProducer = jmsFactory.createProducerQueue(session, tq);
                    messageConsumer = jmsFactory.createConsumerQueue(session, hq);
                } else {
                    messageProducer = jmsFactory.createProducerQueue(session, hq);
                    messageConsumer = jmsFactory.createConsumerQueue(session, tq);
                }
                exchangeValues(leftWorker, session, messageProducer, messageConsumer);
                sendBackAndReceive(leftWorker, messageProducer, messageConsumer, session);

                //Nu heeft de leftworker enkel getallen (coins) die kleiner zijn dan alle getallen van de rightworker
                //Tijd om de officer te berichten dat alles goed verlopen is

                System.out.println("------------------------- WE SEND MESSAGE TO OFFICER -----");
                MessageProducer officerProducer = jmsFactory.createProducerQueue(session, "officer");
                officerProducer.send(session.createObjectMessage(new WorkerTextMessage("done_merging", String.valueOf(workerId))));
                MessageConsumer officerMessageConsumer = jmsFactory.createConsumerQueue(session, "message-server-" + workerId);
                if (numClients >= 4) {
                    String queueToConsume = null;
                    String queueToProduce = null;

                    while (queueToConsume == null || queueToProduce == null && !specialWorker) {

                        WorkerTextMessage receivedMessage = (WorkerTextMessage) ((ObjectMessage) officerMessageConsumer.receive()).getObject();
                        if (receivedMessage.getMessageType().equals("consume")) {
                            queueToConsume = receivedMessage.getMessage();
                        } else if (receivedMessage.getMessageType().equals("produce")) {
                            queueToProduce = receivedMessage.getMessage();
                        }
                    }

                    MessageProducer messageProducer1 = jmsFactory.createProducerQueue(session, queueToProduce);
                    MessageConsumer messageConsumer1 = jmsFactory.createConsumerQueue(session, queueToConsume);
                    leftWorker = !queueToConsume.contains("tail");
                    exchangeValues(leftWorker, session, messageProducer1, messageConsumer1);
                    sendBackAndReceive(leftWorker, messageProducer1, messageConsumer1, session);

                    //Notify the officer that I'm done with merging step 2.
                    officerProducer.send(session.createObjectMessage(new WorkerTextMessage("done_merging_step2", String.valueOf(workerId))));


                    boolean stopWithProcess = false;
                    MessageConsumer step3Consumer = null;
                    MessageProducer step3Producer = null;
                    boolean skipStep3 = false;
                    boolean left = false;
                    while (!stopWithProcess) {

                        if (step3Consumer == null || step3Producer == null) {
                            WorkerTextMessage officerMessage = (WorkerTextMessage) ((ObjectMessage) officerMessageConsumer.receive()).getObject();
                            if (officerMessage.getMessageType().equals("continue")) {
                                stopWithProcess = officerMessage.getMessage().equals("true");
                                skipStep3 = stopWithProcess;
                            } else if (officerMessage.getMessageType().equals("produce")) {
                                String receivedQueueToProduce = officerMessage.getMessage();
                                left = !receivedQueueToProduce.contains("head");
                                step3Producer = jmsFactory.createProducerQueue(session, receivedQueueToProduce);
                            } else if (officerMessage.getMessageType().equals("consume")) {
                                String receivedQueueToConsume = officerMessage.getMessage();
                                step3Consumer = jmsFactory.createConsumerQueue(session, receivedQueueToConsume);
                            }
                        } else {
                            stopWithProcess = true;
                        }
                    }
                    if (!skipStep3) {
                        exchangeValues(left, session, step3Producer, step3Consumer);
                        sendBackAndReceive(left, step3Producer, step3Consumer, session);
                    }
                }

                //Worker stuurt zijn inmiddels gesorteerde lijst naar merged-Alpha queue, maar enkel als hij een bericht krijgt van de Officer.
                //Op deze manier komen de berichten gesorteerd aan bij de officer.
                String queueName = "merged-Alpha";
                boolean doneWithEntireProcess = false;
                while (!doneWithEntireProcess) {
                    WorkerTextMessage officerMessage = (WorkerTextMessage) ((ObjectMessage) officerMessageConsumer.receive()).getObject();
                    if (officerMessage.getMessageType().equals("send-to-alpha")) {
                        //Stuur linkedList naar merged-Alpha queue
                        MessageProducer coinListProducer = jmsFactory.createProducerQueue(session, queueName);
                        coinListProducer.send(session.createObjectMessage(new CoinListMessage(myCoinList.giveHeadElement())));
                        doneWithEntireProcess = true;
                    }
                }
            } else {
                MessageProducer coinListProducer = jmsFactory.createProducerQueue(session, "merged-Alpha");
                coinListProducer.send(session.createObjectMessage(new CoinListMessage(myCoinList.giveHeadElement())));
            }
        }
    }

    //Methode die het teruggeven van coins afhandeld. Wanneer de headWorker een Coin heeft gegeven aan de tailWorker die kleiner is dan de
    //ontvangen coin wordt deze methode aangeroepen en vice versa.
    private static void sendBackAndReceive(boolean leftWorker, MessageProducer mp, MessageConsumer mc, Session session) throws JMSException {
        if (leftWorker && sendBack != null) {
            mp.send(session.createObjectMessage(sendBack));
            Coin receivedCoinBack = (Coin) ((ObjectMessage) mc.receive()).getObject();
            myCoinList.addTail(receivedCoinBack);
        } else if (!leftWorker && sendBack != null) {
            mp.send(session.createObjectMessage(sendBack));
            Coin receivedCoinBack = (Coin) ((ObjectMessage) mc.receive()).getObject();
            myCoinList.addHead(receivedCoinBack);
        }
    }

    //Methode voor het uitwisselen van Coin tussen messageProducers en messageConsumers.
    private static void exchangeValues(boolean leftWorker, Session session, MessageProducer mp, MessageConsumer mc) throws JMSException {
        boolean done = false;
        Coin receivedCoin = null;
        Coin sendCoin = null;
        while (!done) {
            if (leftWorker) {
                sendCoin = myCoinList.giveTailElementAndRemove();
                //Stuur de grootste Coin van mijn eigen gesorteerde lijst weg
                mp.send(session.createObjectMessage(sendCoin));
                //Ontvang van een andere worker zijn laagste Coin
                receivedCoin = (Coin) ((ObjectMessage) mc.receive()).getObject();
            } else {
                sendCoin = myCoinList.giveHeadElementAndRemove();
                //Stuur de laagste Coin van mijn eigen gesorteerde lijst weg
                mp.send(session.createObjectMessage(sendCoin));
                //Ontvang van een andere worker zijn laagste Coin
                receivedCoin = (Coin) ((ObjectMessage) mc.receive()).getObject();

            }
            done = processExchangedValues(sendCoin, receivedCoin, leftWorker);
        }
    }

    //Methode voor het vergelijken van de gegeven Coin en de ontvanagen Coin.
    private static boolean processExchangedValues(Coin sentCoin, Coin receivedCoin, boolean isLeftWorker) {
        if (isLeftWorker) {
            if (sentCoin.compareTo(receivedCoin) < 0) {
                sendBack = receivedCoin;
                return true;
            }
            if (receivedCoin.compareTo(myCoinList.giveHeadElement().c) < 0) {
                myCoinList.addHead(receivedCoin);
                return false;
            }
            myCoinList.insert(receivedCoin);
        } else {
            if (sentCoin.compareTo(receivedCoin) > 0) {
                sendBack = receivedCoin;
                return true;
            }
            if (receivedCoin.compareTo(myCoinList.giveTailElement().c) > 0) {
                myCoinList.addTail(receivedCoin);
                return false;
            }
            myCoinList.insert(receivedCoin);
        }
        return false;
    }

    //Genereert een lijst aan ongesorteerde Coin elementen
    public static List<Coin> generateCoinObjects(int workerId) {
        return new DataGenerator(AMOUNT_OF_ELEMENTS).generate(workerId);
    }

    //Bepaal op welke queueID de worker moet gaan procuden/consumen
    private static int determineQueueId(int workerId) {
        if (workerId <= 2) {
            return 0;
        } else {
            return 1;
        }

    }

    private static String getHeadQueue(int workerId) {
        return "head" + determineQueueId(workerId);
    }

    private static String getTailQueue(int workerId) {
        return "tail" + determineQueueId(workerId);
    }


}
