import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class HashmapMessage implements Serializable {

    private Map<Integer, Map<String, Coin>>  map;

    public HashmapMessage(Map<Integer,Map<String, Coin>>  map){
        this.map = map;
    }

    public Map<Integer,Map<String, Coin>>  getMessage(){
        return map;
    }
}
