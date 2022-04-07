import java.io.Serializable;

public class NodeMessage implements Serializable {
        private final String message;
        private final String type;

        public NodeMessage(String type, String message) {
            this.message = message;
            this.type = type;

        }

        public String getMessage() {
            return message;
        }
        public String getMessageType() { return type;}


    }


