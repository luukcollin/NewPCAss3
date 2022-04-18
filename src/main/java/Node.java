import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Node implements Serializable {
    Coin c;
    Node prev, next;

    Node(Coin c){
        this.c = c;
        next = null;
        prev = null;
    }

    //Het onthullen van een Node zijn stamboom wordt enkel gebruikt voor SortTest.java voor test purposes
    public List<Coin> revealGenealogy(){
        List<Coin> allCoinsInNodeList = new ArrayList<>();
        Node temp = this;
        while(temp != null){
            allCoinsInNodeList.add(temp.c);
            System.out.print(temp.c.toString());
            temp = temp.next;

        }
        return allCoinsInNodeList;
    }

}
