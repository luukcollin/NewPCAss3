import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Sort {
    List<Coin[]> smallerArrays;
    Coin[] sortedData;
    public Sort(){
        this.smallerArrays = new ArrayList<>();
    }

    public List<Coin> sort(List<Coin> unsorted) {
        if(unsorted.size() > 0) {
            mergesort(unsorted, 0, unsorted.size());
            List<Coin[]> sortedArray = smallerArrays;
            return new ArrayList<Coin>(Arrays.asList(sortedArray.get(smallerArrays.size() - 1)));
        }
        return unsorted;
    }


    /**
     * Simple recursive mergesort algorithm that I found on https://www.programmersought.com/article/93992485097/
     * I built my own merge sort algo first, but then I decided to refactor it into this code.
     * @param data the data to be sorted
     * @param lo lowest specified index, in most cases 0.
     * @param hi max index of the data array
     */
    private void mergesort(List<Coin> data, int lo, int hi) {
        if (hi - lo < 2)
            return;
        int mi = (lo + hi) / 2;
        mergesort(data, mi, hi );
        mergesort(data, lo, mi );
        merge(data, lo, mi, hi);
    }

    /**
     *
     * @param data the data to be sorted
     * @param lo lowest specified index, in most cases 0.
     * @param mi middle index of the data array
     * @param hi max index of the data array
     */
    private void merge(List<Coin> data, int lo, int mi, int hi) {
        // index i1 as a temp, i2 as the array data [lo, mi - 1] subscript, i3 as the array data [mi, hi - 1] subscript
        int i1 = 0, i2 = lo, i3 = mi;
        Coin[] temp = new Coin[hi - lo];

        while ((i2 < mi) || (i3 < hi)) {
            if ((i2 < mi) && (!(i3 < hi) || (data.get(i2).compareTo(data.get(i3)) <= 0))){
                temp[i1++] = data.get(i2++);
            }
            if ((i3 < hi) && (!(i2 < mi) || data.get(i2).compareTo (data.get(i3)) > 0)) {
                temp[i1++] = data.get(i3++);
            }
        }

        for (int i = 0; i < hi - lo; i++) {
            data.set(i + lo, temp[i]);
        }

        sortedData = temp;
        //Add the merged data to Arraylist
        smallerArrays.add(temp);
        //To merge the sorted halves do: this.smallerArrays.get((this.smallerArrays.size()/2 - 1) + this.smallerArrays.get((this.smallerArrays.size() - 1)

    }
}
