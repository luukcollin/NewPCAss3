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
       for(int i = 0; i < nodes.size(); i++){
           for(int j = i+1; j < nodes.size(); j++){
               if(nodes.get(i).c.compareTo(nodes.get(j).c) > 0){
                   Node temp = nodes.get(i);
                   nodes.set(i, nodes.get(j));
                   nodes.set(j, temp);

               }
           }
       }
       return nodes;
    }
}
