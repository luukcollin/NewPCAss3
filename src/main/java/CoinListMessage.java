import java.io.Serializable;
import java.util.List;

public class CoinListMessage implements Serializable {

    private Node head;

    public CoinListMessage(Node node) {
        this.head = node;
    }

    public Node getNode() {
        return head;
    }
}