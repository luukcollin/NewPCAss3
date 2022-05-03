import java.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


public class LinkedListTest {
    private static final Coin coinObject1 = new Coin(45984.5412165, 100, 58331.30, new GregorianCalendar(2022, Calendar.FEBRUARY, 12), 1);
    private static final Coin coinObject2 = new Coin(294178.08074959525, 50, 58331.30, new GregorianCalendar(2022, Calendar.FEBRUARY, 12), 2);
    private static final Coin coinObject3 = new Coin(794546178.08074959525, 450, 58331.30, new GregorianCalendar(2022, Calendar.FEBRUARY, 12), 3);
    private static final Coin coinObject4 = new Coin(787465106.5465, 250, 58331.30, new GregorianCalendar(2022, Calendar.FEBRUARY, 12), 4);
    private static final Coin coinObject5 = new Coin(787465106.5465, 250, 58331.30, new GregorianCalendar(2022, Calendar.FEBRUARY, 12), 5);
    private static final Coin coinObject6 = new Coin(45984.5412165, 20, 58331.30, new GregorianCalendar(2022, Calendar.FEBRUARY, 12), 6);
    private static final Coin coinObject7 = new Coin(294178.08074959525, 350, 58331.30, new GregorianCalendar(2022, Calendar.FEBRUARY, 12), 7);
    private static final Coin coinObject8 = new Coin(794546178.08074959525, 150, 58331.30, new GregorianCalendar(2022, Calendar.FEBRUARY, 12), 8);
    private static final Coin coinObject9 = new Coin(787465106.5465, 150, 58331.30, new GregorianCalendar(2022, Calendar.FEBRUARY, 12), 9);
    private static final Coin coinObject10 = new Coin(787465106.5465, 220, 58331.30, new GregorianCalendar(2022, Calendar.FEBRUARY, 12), 10);
    private static final List<Coin> unsortedList1 = new ArrayList<>(Arrays.asList(coinObject1, coinObject2, coinObject3, coinObject4, coinObject5));
    private static final List<Coin> unsortedList2 = new ArrayList<>(Arrays.asList(coinObject6, coinObject7, coinObject8, coinObject9, coinObject10));
    private static final LinkedList listWhereTheMergeSortMagicHappens = new LinkedList();
    private static final LinkedList listWhereTheMergeSortMagicHappens2 = new LinkedList();

    //Expected result after sorting unsortedList1, and unsortedList2 and after completed the merging of those sorted lists
    private static final List<Coin> COMPLETELY_SORTED_AND_MERGED = new ArrayList<>(
            Arrays.asList(coinObject6, coinObject2, coinObject1, coinObject9, coinObject8, coinObject10, coinObject4,
                    coinObject5, coinObject7, coinObject3));

    @BeforeAll
    public static void init(){
        //Voeg elementen toe aan de linkedlists zodat deze gesorteerd kunnen worden a.d.h.v. geimplementeerde merge sort algo
        for(Coin c : unsortedList1){
            //Voeg coins toe aan de voorkant van de lijst. Voor dit geval maakt het niet uit of we voor of achteraan toevoegen, omdat
            //alle coins die we nu toevoegen NIET op volgorde staan
            listWhereTheMergeSortMagicHappens.addHead(c);
        }
        for(Coin c: unsortedList2){
            //Idem
            listWhereTheMergeSortMagicHappens2.addHead(c);
        }
    }


    @Test
    public  void sortTest(){
        List<Coin> EXPECTED = new ArrayList<>(
                Arrays.asList(coinObject6, coinObject2, coinObject1, coinObject9, coinObject8, coinObject10, coinObject4,
                        coinObject5, coinObject7, coinObject3));

        listWhereTheMergeSortMagicHappens.sortList();
        listWhereTheMergeSortMagicHappens2.sortList();

        Node first = listWhereTheMergeSortMagicHappens.giveHeadElement();
        Node second = listWhereTheMergeSortMagicHappens2.giveHeadElement();

        Node merged = new LinkedList().merge(first, second);

        //De gehele nodelist wordt omgezet naar een ArrayList enkel voor testing purposes om gemakkelijker te achterhalen
        //of alle elementen in de lijst zitten
        System.out.println("This is how the sorted list should look like. [it's sorted by price, then marketcap, followed by the coin id]");
        List<Coin> mergedList = merged.revealGenealogy();
        Assertions.assertEquals(EXPECTED, mergedList);

        //Testing additional sorting method for a node as additional check
        Assertions.assertTrue(new Sort().isSorted(merged));
    }

    @Test
    public void insertSorted(){
        //Twee coinobjecten die allebei net niet de hoogste 'price'. Bij het inserten zouden deze dus op de een na laatste
        //index geplaatst worden en coinObjectX op de index daarvoor.
        Coin coinObjectX = new Coin(1000.000000, 400, 58331.30, new GregorianCalendar(2022, Calendar.FEBRUARY, 12), 11);
        Coin coinObjectY = new Coin(1000.000000, 420, 58331.30, new GregorianCalendar(2022, Calendar.FEBRUARY, 12), 12);

        LinkedList coinList = new LinkedList();
        for(Coin c : COMPLETELY_SORTED_AND_MERGED){
            coinList.addTail(c);
        }

        Assertions.assertEquals(COMPLETELY_SORTED_AND_MERGED.get(0), coinList.giveHeadElement().c);
        Assertions.assertEquals(COMPLETELY_SORTED_AND_MERGED.get(COMPLETELY_SORTED_AND_MERGED.size()-1), coinList.giveTailElement().c);

        coinList.insert(coinObjectX);
        coinList.insert(coinObjectY);


        Assertions.assertEquals(coinList.giveHeadElement().c, coinObject6);
        Assertions.assertEquals(coinList.giveTailElement().c, coinObject3);

        List<Coin> EXPECTED = new ArrayList<>(
                Arrays.asList(coinObject6, coinObject2, coinObject1, coinObject9, coinObject8, coinObject10, coinObject4,
                        coinObject5, coinObject7, coinObjectX, coinObjectY, coinObject3));
        List<Coin> result = new ArrayList<>();
        while(coinList.giveHeadElement() != null){
            result.add(coinList.giveHeadElementAndRemove());
        }
        Assertions.assertEquals(EXPECTED, result);
    }

    @Test
    public void deleteTail(){
        List<Coin> unsortedList1 = new ArrayList<>(Arrays.asList(coinObject1, coinObject2, coinObject3, coinObject4, coinObject5));


        LinkedList mylist = new LinkedList();
        for(Coin c: unsortedList1){
            mylist.addTail(c);
        }
        Assertions.assertSame(mylist.giveTailElement().c, coinObject5);
        mylist.giveTailElementAndRemove();
        Assertions.assertSame(mylist.giveTailElement().c, coinObject4);

    }

    @Test
    public void emptyList(){
        LinkedList emptylist = new LinkedList();

        Assertions.assertNull(emptylist.giveHeadElement());
        Assertions.assertNull(emptylist.giveTailElement());
    }

    @Test
    public void returnHeadWhenTailIsEmpty(){
        LinkedList singleElementList = new LinkedList();
        singleElementList.addHead(coinObject1);

        //Calls on empty list
        //Assert 'null' as return and be free of any Nullpointer exceptions
        Assertions.assertEquals(coinObject1, singleElementList.giveTailElement().c);
        Assertions.assertEquals(coinObject1, singleElementList.giveTailElementAndRemove());
        Assertions.assertNull(singleElementList.giveTailElement());
        Assertions.assertNull(singleElementList.giveTailElementAndRemove());

    }

    @Test
    public void returnHeadWhenTailWasAddedAsOnlyElement(){
        LinkedList singleElementList = new LinkedList();
        singleElementList.addHead(coinObject1);

        Assertions.assertEquals(coinObject1, singleElementList.giveHeadElementAndRemove());

        //Calls on empty list
        //Assert 'null' as return and be free of any Nullpointer exceptions
        Assertions.assertNull(singleElementList.giveHeadElement());
        Assertions.assertNull(singleElementList.giveHeadElementAndRemove());



    }

    @Test
    public void insertTest(){
        LinkedList coinList = new LinkedList();
        for(Coin c : COMPLETELY_SORTED_AND_MERGED){
            coinList.addTail(c);
        }

        Assertions.assertEquals(COMPLETELY_SORTED_AND_MERGED.get(0), coinList.giveHeadElement().c);
        Assertions.assertEquals(COMPLETELY_SORTED_AND_MERGED.get(COMPLETELY_SORTED_AND_MERGED.size()-1), coinList.giveTailElement().c);

        Coin newHead = new Coin(1111111.5412165, 100, 156700.30, new GregorianCalendar(2022, Calendar.FEBRUARY, 5), 77);
        Coin newTail = new Coin(9999999.5412165, 500, 80087612.30, new GregorianCalendar(2022, Calendar.MARCH, 12), 99);
        coinList.addHead(newHead);
        coinList.addTail(newTail);

        Assertions.assertEquals(coinList.giveHeadElement().c, newHead);
        Assertions.assertEquals(coinList.giveTailElement().c, newTail);
    }

    @Test
    public void addFirstNode(){
        //Check for addHead errors (f.e. NullPointer) on first Node in list
        LinkedList newList =  new LinkedList();
        newList.addHead(coinObject1);

        //Check for addTail errors on first Node in list
        LinkedList newList2 =  new LinkedList();
        newList2.addTail(coinObject1);
    }

}
