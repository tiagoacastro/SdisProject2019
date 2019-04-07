package channels;

import code.Chunk;
import code.MessageFactory;
import code.Peer;

import java.net.InetAddress;
import java.net.MulticastSocket;

public class Mdb extends Channel{
    public static InetAddress address;
    public static int port;
    public static MulticastSocket socket;

    public Mdb (String addr, int port){
        Mdb.address = getAddress(addr);
        Mdb.port = port;
        Mdb.socket = getMCSocket(address, port);
    }

    @Override
    public void run() {
        while(true) {
            String message = getPacketMessage(socket);
            if (message != null) {
                String[] tokens = message.split(" ");
                if (Integer.parseInt(tokens[2]) != Peer.senderId && tokens[0].equals("PUTCHUNK")) {
                    String[] params = new String[]{tokens[3], tokens[4]};
                    message = MessageFactory.addHeader("STORED", params);
                    sendPacket(Mc.socket, message, Mc.address, Mc.port);
                }
            }
        }
    }
}
