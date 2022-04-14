import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Sort {
    //Return is a Node is sorted
    public boolean isSorted(Node coin){
        Node temp = coin;
        while(temp.next != null && temp.c.compareTo(temp.next.c) < 0){
            temp = temp.next;
            if(temp.next.next == null){
                return true;
            }
        }
        return false;
    }
}
