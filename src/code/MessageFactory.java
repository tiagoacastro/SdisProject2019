package code;

public class MessageFactory {
    public static String addHeader(String type, String[] params) {
        StringBuilder result = new StringBuilder();

        for (int i = 1; i < params.length; i++) {
            result.append(params[i]);
            result.append(" ");
        }

        return type + " " +
                Peer.version + " " +
                Peer.senderId + " " +
                params[0] + " " +
                result.toString() +
                "\r\n\r\n";
    }
}
