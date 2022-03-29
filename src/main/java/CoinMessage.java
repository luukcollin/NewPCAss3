import java.io.Serializable;


public class CoinMessage implements Serializable {

    private Coin coin;

    public CoinMessage(Coin coin){
        this.coin = coin;
    }

    public Coin getMessage(){
        return coin;
    }
}
