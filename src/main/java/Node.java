import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Node implements Serializable {
    Coin c;
    Node next;

    Node(Coin c) {
        this.c = c;
        next = null;
    }

    //Het onthullen van een Node zijn stamboom. Geeft een lijst van coins.
    public List<Coin> revealGenealogy() {
        List<Coin> allCoinsInNodeList = new ArrayList<>();
        Node temp = this;
        while (temp != null) {
            allCoinsInNodeList.add(temp.c);
            temp = temp.next;
        }
        return allCoinsInNodeList;
    }
}

