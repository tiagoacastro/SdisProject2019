package code;

public class MessageFactory {
    public static String addHeader(String type, String[] params) {
        String message = type + " " +
                Peer.version + " " +
                Peer.senderId + " " +
                params[0] + " ";

        for (int i = 1; i < params.length; i++) {
                message += params[i] + " ";
        }

        message += "\r\n\r\n";

        return message;
    }
}
