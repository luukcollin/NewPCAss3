import java.util.*;

public class DataGenerator {
    private int amountOfElements;
    public DataGenerator(int amountOfElements){
        this.amountOfElements = amountOfElements;


    }

    public List<Coin> generate(){
        Random random = new Random();
        List<Coin> allCoins = new ArrayList<>();
        String[] months = new String[]{"JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE", "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"};
        double MINIMUMCAPVALUE = 1_000.0000;
        double MAXIMUMCAPVALUE = 2_000_000.0000;

        double MINIMUMPRICEVALUE = .0001;
        double MAXIMUMPRICEVALUE = 200_0.0000;

        double MINIMUMDAILYVOLUME = 100.0000;
        double MAXIMUMDAILYVOLUME = 10_000_000_00.0000;

        int MINIMUMYEAR = 2009;
        int MAXIMUMYEAR = 2020;

        int MINIMUMDATE = 1;
        int MAXIMUMDATE = 28;


        double coinMarketCap;
        double coinPrice;
        double coinDailyVolume;
        int year;
        int date;
        for(int i = 0; i < amountOfElements; i++){


            coinMarketCap = MINIMUMCAPVALUE + (Math.random() * ((MAXIMUMCAPVALUE - MINIMUMCAPVALUE) +1));
            coinPrice = MINIMUMPRICEVALUE + (MAXIMUMPRICEVALUE - MINIMUMPRICEVALUE) * random.nextDouble();
            coinDailyVolume = MINIMUMDAILYVOLUME + (Math.random() * ((MAXIMUMDAILYVOLUME - MINIMUMDAILYVOLUME) +1));
            year = MINIMUMYEAR + (int)(Math.random() * ((MAXIMUMYEAR - MINIMUMYEAR) +1));
            date = MINIMUMDATE + (int)(Math.random() * ((MAXIMUMDATE - MINIMUMDATE) +1));

            allCoins.add(new Coin(coinMarketCap, coinPrice, coinDailyVolume, new GregorianCalendar( (1900+year), Calendar.FEBRUARY, date ), i));
        }
        return allCoins;
    }
}
