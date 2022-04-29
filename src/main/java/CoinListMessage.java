import java.io.Serializable;

public class CoinListMessage implements Serializable {
    private final Node head;
    public CoinListMessage(Node node) {
        this.head = node;
    }

    public Node getNode() {
        return head;
    }
}