import java.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class SortTest {
    private static Coin coinObject1 = new Coin(45984.5412165, 100, 58331.30, new GregorianCalendar(2022, Calendar.FEBRUARY, 12), 1);
    private static Coin coinObject2 = new Coin(294178.08074959525, 50, 58331.30, new GregorianCalendar(2022, Calendar.FEBRUARY, 12), 2);
    private static Coin coinObject3 = new Coin(794546178.08074959525, 450, 58331.30, new GregorianCalendar(2022, Calendar.FEBRUARY, 12), 3);
    private static Coin coinObject4 = new Coin(787465106.5465, 250, 58331.30, new GregorianCalendar(2022, Calendar.FEBRUARY, 12), 4);
    private static Coin coinObject5 = new Coin(787465106.5465, 250, 58331.30, new GregorianCalendar(2022, Calendar.FEBRUARY, 12), 5);
    ;
    private static Coin coinObject6 = new Coin(45984.5412165, 20, 58331.30, new GregorianCalendar(2022, Calendar.FEBRUARY, 12), 6);
    private static Coin coinObject7 = new Coin(294178.08074959525, 350, 58331.30, new GregorianCalendar(2022, Calendar.FEBRUARY, 12), 7);
    private static Coin coinObject8 = new Coin(794546178.08074959525, 150, 58331.30, new GregorianCalendar(2022, Calendar.FEBRUARY, 12), 8);
    private static Coin coinObject9 = new Coin(787465106.5465, 150, 58331.30, new GregorianCalendar(2022, Calendar.FEBRUARY, 12), 9);
    private static Coin coinObject10 = new Coin(787465106.5465, 220, 58331.30, new GregorianCalendar(2022, Calendar.FEBRUARY, 12), 10);

    private static List<Coin> unsortedList1 = new ArrayList<Coin>(Arrays.asList(coinObject1, coinObject2, coinObject3, coinObject4, coinObject5));
    private static List<Coin> unsortedList2 = new ArrayList<Coin>(Arrays.asList(coinObject6, coinObject7, coinObject8, coinObject9, coinObject10));

    //Expected result after sorting unsortedList1, and unsortedList2 and after completed the merging of those sorted lists
    private static List<Coin> COMPLETELY_SORTED_AND_MERGED = new ArrayList<Coin>(
            Arrays.asList(coinObject6, coinObject2, coinObject1, coinObject9, coinObject8, coinObject10, coinObject4,
                    coinObject5, coinObject7, coinObject3));

    @Test
    public void sortMultipleCoinListsAndMergeTogetherSorted() {
        Sort sort = new Sort();
        List<Coin> sortedList1 = sort.sort(unsortedList1);
        List<Coin> sortedList2 = sort.sort(unsortedList2);

        Merger merger = new Merger();
        List<Coin> result = merger.mergeInOrderWithPivot(sortedList1, sortedList2);

        for (Coin c :
                result) {
            System.out.println(c.toString());
        }
        Assertions.assertEquals(result, COMPLETELY_SORTED_AND_MERGED);
    }
}



