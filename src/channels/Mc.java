package channels;

import code.Peer;
import code.StoreRequest;

import java.io.File;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Mc extends Channel{
    public static InetAddress address;
    public static int port;
    public static MulticastSocket socket;

    public Mc (String addr, int port){
        Mc.address = getAddress(addr);
        Mc.port = port;
        Mc.socket = getMCSocket(address, port);
    }

    @Override
    public void run() {
        while(true) {
            byte[] msg = getPacketMessage(socket);;
            String message = new String(msg).replaceAll("\0", "");

            if (message != null) {
                String[] tokens = message.split(" ");
                if (Integer.parseInt(tokens[2]) != Peer.senderId)
                    switch(tokens[0]){
                        case "STORED":
                            StoreRequest req = Peer.requests.get(tokens[3]);
                            req.store(Integer.parseInt(tokens[4]), Integer.parseInt(tokens[2]));
                            break;
                        case "DELETE":
                            File directory = new File(tokens[3]);
                            if (directory.exists()) {
                                String[] files = directory.list();

                                for(String s: files) {
                                    File currentFile = new File(directory.getPath(), s);
                                    currentFile.delete();
                                }

                                directory.delete();
                            }
                            break;
                    }
            }
        }
    }
}

