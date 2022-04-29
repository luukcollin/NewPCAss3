import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ListTest {
    public ListTest(){

    }
    public boolean isSorted(List<Coin> coinList){
        for(int i = 0; i< coinList.size()-1; i++){
            Coin current = coinList.get(i);
            if(coinList.get(i).compareTo(current) < 0) return false;
        }
        return true;
    }
    public boolean containsOnlyUniqueElements(List<Coin> coinList){
        // Put all array elements in a HashSet
        Set<Coin> coinSet =
                new HashSet<>(coinList);
        return coinSet.size() == coinList.size();
    }

    public boolean listHasExpectedSize(List<Coin> coinList, int amountOfWorkers, int elementsPerWorker){
        return coinList.size() == amountOfWorkers * elementsPerWorker;
    }

}
