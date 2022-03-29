import java.util.ArrayList;
import java.util.List;

public class Merger {
    public List<Coin> mergeInOrderWithPivot(List<Coin> sortedList1, List<Coin> sortedList2){
        List<Coin> listWithBiggerElements, listWithSmallerElements;
        List<Coin> sorted = new ArrayList<>();
        int i = 0, j=0;
        int mid1 =(int) Math.floor(sortedList1.size()/2);
        int mid2 =(int) Math.floor(sortedList2.size()/2);
        if(sortedList1.get(mid1).compareTo(sortedList2.get(mid2)) < 0){
            //Pivot of sortedList1 is smaller than the pivot of sortedList2. Which means that most likely sortedList2
            //contains more bigger elements.
            //Therefore, it's most likely better to iterate sortedList1 from 0 to ..1000 and iterate through sortedList2
            //starting from highest index to 0.
            listWithBiggerElements  = sortedList2;
            listWithSmallerElements = sortedList1;
        }else{ //I'm ignoring the case where the pivots of both lists are equal, because it means that we can't determine if it's faster to iterate sortedList1 or sortedList2 backwards.
            listWithBiggerElements = sortedList1;
            listWithSmallerElements = sortedList2;
        }
        while(i < listWithBiggerElements.size() && j < listWithSmallerElements.size()){
            //Case listWithSmallerElements(element) is smaller than ...
            if(listWithSmallerElements.get(i).compareTo(listWithBiggerElements.get(j)) < 0){
                sorted.add(listWithSmallerElements.get(i++));
            }else if(listWithSmallerElements.get(i).compareTo(listWithBiggerElements.get(j)) > 0){
                sorted.add(listWithBiggerElements.get(j++));
            }else{ //Elements are equal
                sorted.add(listWithSmallerElements.get(i++));
                sorted.add(listWithBiggerElements.get(j++));


            }

        }
        // Store remaining elements of first array
        while (i < listWithSmallerElements.size()){
            sorted.add(listWithSmallerElements.get(i++));
        }
        // Store remaining elements of second array
        while (j < listWithBiggerElements.size()){
            sorted.add(listWithBiggerElements.get(j++));
        }


        return sorted;
    }
}
