import java.util.ArrayList;
import java.util.Arrays;
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
}
