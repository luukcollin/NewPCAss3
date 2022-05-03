import java.io.Serializable;
import java.util.Comparator;
import java.util.GregorianCalendar;

public class Coin implements Comparable<Coin>, Serializable {

    private final double marketCap;
    private final double price;
    private final double dailyVolume;
    private final GregorianCalendar launchedAt;
    private final int id;

    public Coin(double marketCap, double price, double dailyVolume, GregorianCalendar launchedAt, int id){
        this.marketCap = marketCap;
        this.price = price;
        this.dailyVolume = dailyVolume;
        this.launchedAt = launchedAt;
        this.id = id;
    }

    @Override
    public int compareTo(Coin o) {
        return Comparator.comparing((Coin c) -> c.marketCap).thenComparing(c -> c.price ).thenComparing(c -> c.id)
                .thenComparing(c -> c.launchedAt).compare(this, o);
    }

    @Override
    public String toString(){
        return "( Coin ID " + id +  "   -   Marketcap "+ String.format("%.2f", marketCap) + marketCap   +  "   -   Price " + String.format("%.2f", price) + " euro   -   Daily Volume " + String.format("%.2f",dailyVolume) +   " euro )\n";
    }



}
