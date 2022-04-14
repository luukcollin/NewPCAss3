import java.io.Serializable;

public class WorkerTextMessage implements Serializable {
    private final String type;
    private final String message;
    public WorkerTextMessage(String type, String message){
        this.type = type;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
    public String getMessageType() { return type;}

}
