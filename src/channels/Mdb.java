package channels;

import code.Chunk;
import code.MessageFactory;
import code.Peer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

                    FileOutputStream out = null;

                    File directory = new File("file_" + tokens[3]);
                    if (! directory.exists())
                        directory.mkdir();

                    try {
                        out = new FileOutputStream("file_" + tokens[3] + "/" + tokens[4]);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        for(int i = 7; i < tokens.length; i++) {
                            out.write((tokens[i] + " ").getBytes());
                        }

                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String[] params = new String[]{tokens[3], tokens[4]};
                    message = MessageFactory.addHeader("STORED", params);
                    sendPacket(Mc.socket, message, Mc.address, Mc.port);
                }
            }
        }
    }
}
