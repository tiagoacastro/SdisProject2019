package channels;

import code.Peer;
import code.Request;

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
            String message;
            message = getPacketMessage(socket);
            if (message != null) {
                String[] tokens = message.split(" ");
                if (Integer.parseInt(tokens[2]) != Peer.senderId && tokens[0].equals("STORED")) {
                    Request req = Peer.requests.get(tokens[3]);
                    req.store(Integer.parseInt(tokens[4]), Integer.parseInt(tokens[2]));
                }
            }
        }
    }
}

