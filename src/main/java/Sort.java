import java.util.ArrayList;
import java.util.List;

public class Sort {
    //Return is a Node is sorted
    public boolean isSorted(Node coin){
        Node current = coin;
        if(current == null || current.next == null){
            return true;
        }
        while(current.next != null){
          if(current.c.compareTo(current.next.c) > 0) return false;
          current = current.next;
        }
        return true;
    }

    public List<Node> sortNodesEasily(List<Node> nodes){
        int n = nodes.size();
        for (int i = 0; i < n - 1; i++)
            for (int j = 0; j < n - i - 1; j++){
                if (nodes.get(j).c.compareTo(nodes.get(j  + 1).c) > 0) {
                    // swap arr[j+1] and arr[j]
                    Node temp = nodes.get(j);
                    nodes.set(j, nodes.get(j + 1));
                    nodes.set(j + 1, temp);
                }
       }
       return nodes;
    }
}
