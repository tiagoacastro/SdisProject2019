package channels;

import code.Peer;
import code.StoreRequest;

import java.io.File;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;

public class Mc extends Channel{
    public static InetAddress address;
    public static int port;
    public static MulticastSocket socket;
    private static HashMap<String, StoreRequest> requests;

    public Mc (HashMap<String, StoreRequest> requests, String addr, int port){
        Mc.address = getAddress(addr);
        Mc.port = port;
        Mc.socket = getMCSocket(address, port);
        Mc.requests= requests;
    }

    @Override
    public void run() {
        while(true) {
            byte[] msg = getPacketMessage(socket);
            if(msg != null) {
                String message = new String(msg).replaceAll("\0", "");

                if (message != null) {
                    String[] tokens = message.split(" ");
                    if (Integer.parseInt(tokens[2]) != Peer.senderId)
                        switch (tokens[0]) {
                            case "STORED":
                                StoreRequest req = requests.get(tokens[3]);
                                req.store(Integer.parseInt(tokens[4]), Integer.parseInt(tokens[2]));
                                break;
                            case "DELETE":
                                File directory = new File("Files/" + tokens[3]);
                                if (directory.exists()) {
                                    try {
                                        String[] files = directory.list();
                                        if(files != null){
                                            for (String s : files) {
                                                File currentFile = new File(directory.getPath(), s);
                                                if (!currentFile.delete())
                                                    throw new Exception("couldn't delete file");
                                            }
                                            if (!directory.delete())
                                                throw new Exception("couldn't delete file");
                                        }
                                    } catch(Exception e){
                                        e.printStackTrace();
                                        System.exit(-1);
                                    }
                                }
                                break;
                        }
                }
            }
        }
    }
}

