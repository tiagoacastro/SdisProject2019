package code;

public class MessageFactory {
    public static String addHeader(String type, String[] params) {
        String message = type + " " +
                Peer.version + " " +
                Peer.senderId + " " +
                params[0];

        for (int i = 1; i < params.length; i++) {
            message += " " + params[i];

            if (i == params.length - 2)
                if (type.equals("PUTCHUNK") || type.equals("CHUNK"))
                    break;
        }

        message += " \r\n\r\n";

        if (type.equals("PUTCHUNK") || type.equals("CHUNK"))
            message += " " + params[params.length - 1];

        return message;
    }
}
