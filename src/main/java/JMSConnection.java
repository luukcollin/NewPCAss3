public interface JMSConnection {
    String CONNECTION_URL = "failover:(tcp://localhost:61616)";
    int AMOUNT_OF_ELEMENTS = 500;
}
