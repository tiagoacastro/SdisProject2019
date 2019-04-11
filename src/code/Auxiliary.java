package code;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Auxiliary {
    public static String addHeader(String type, String[] params) {
        StringBuilder result = new StringBuilder();

        for (String param : params) {
            result.append(param);
            result.append(" ");
        }

        return type + " " +
                Peer.version + " " +
                Peer.senderId + " " +
                result.toString() +
                "\r\n\r\n";
    }

    public static String encodeFileId(File file) {
        String originalString = null;
        MessageDigest md = null;
        StringBuilder result = new StringBuilder();

        try {
            originalString = file.getName() + "_" +
                    file.lastModified() + "_" +
                    Files.getOwner(file.toPath());
        } catch (IOException e) {
            System.err.println("Error retrieving file information");
            System.exit(-1);
        }

        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error getting instance of MessageDigest");
            System.exit(-2);
        }

        md.update(originalString.getBytes());

        for (byte byt : md.digest())
            result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));

        return result.toString();
    }
}
