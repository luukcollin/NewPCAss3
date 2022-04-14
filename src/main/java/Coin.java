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
        return Comparator.comparing((Coin c) -> c.price).thenComparing(c -> c.marketCap ).thenComparing(c -> c.id)
                .thenComparing(c -> c.launchedAt).compare(this, o);
    }

    @Override
    public String toString(){
        return "( Coin ID " + id +  " - Price €" + price + " - Marketcap "+  + marketCap + " - Daily Volume €" + dailyVolume +   ")\n";
    }


}
