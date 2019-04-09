package code;

import java.io.*;

public class Main {
    private static Peer peer;

    static class Hook extends Thread {
        @Override
        public void run() {
            System.out.println("hey");
            File directoryPeers = new File("Peers");
            if (!directoryPeers.exists())
                if(!directoryPeers.mkdir())
                    return;

            try {
                FileOutputStream fs = new FileOutputStream("Peers/" + Peer.senderId);
                ObjectOutputStream out = new ObjectOutputStream(fs);
                out.writeObject(peer);
                out.close();
                fs.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        int senderId = Integer.parseInt(args[1]);

        String path = "Peers/" + senderId;
        File f = new File(path);
        if(f.exists() && !f.isDirectory()) {
            try {
                FileInputStream fs = new FileInputStream(path);
                ObjectInputStream in = new ObjectInputStream(fs);
                peer = (Peer)in.readObject();
                in.close();
                fs.close();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        } else
            peer = new Peer();

        Runtime.getRuntime().addShutdownHook(new Hook());
        peer.start(args);
    }
}
