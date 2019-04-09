package code;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class Main {
    private static Peer peer;

    static class Hook extends Thread {
        @Override
        public void run() {
            System.out.println("hey");
            /*try {
                ObjectOutputStream out = new ObjectOutputStream(System.out);
                out.writeObject(peer);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }
    }

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Hook());
        peer = new Peer(args);
    }
}
