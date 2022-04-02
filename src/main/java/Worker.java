import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Worker implements JMSConnection{
    private int workerId;
    private List<Coin> sortedElements;
    private List<Coin> unsortedElements;

    public void main(String[] args){
        workerId = Integer.parseInt(args[0]); //Set workerid
        unsortedElements = this.generateElements();

        System.out.println("Sorting the elements will start");
        Timer.start();
        sortUnsortedElements();
        Timer.stop();
        System.out.println("Worker(" + workerId +")" + ": Sorting the elements is done");

        //Worker subscribet op het Merger Topic wanneer deze zijn ongesorteerde lijst heeft gesorteerd.
        //TODO topic.subscribe(topic)
    }

    public List<Coin> generateElements(){
        return new DataGenerator(AMOUNT_OF_ELEMENTS).generate();
    }

    public void sortUnsortedElements(){
        sortedElements = new Sort().sort(unsortedElements);
    }

    public Map<Integer,Map<String, Coin>> getHiLoAndPivotOfSortedArray(){
        Map<String, Coin> map = new HashMap<>();
        map.put("lo", sortedElements.get(0));
        int lastIndex = sortedElements.size()-1;
        map.put("hi", sortedElements.get(lastIndex));
        map.put("pivct", sortedElements.get(lastIndex/2));

        Map<Integer,Map<String, Coin>> actualReturn = new HashMap<>();
        actualReturn.put(workerId, map);
        return actualReturn;
    }

}
