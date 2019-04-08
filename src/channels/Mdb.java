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

    private void createChunk(byte[] msg, String fileId, String chunkNo)
    {
        FileOutputStream out = null;

        File directory = new File("file_" + fileId);// + msg[3]);
        if (! directory.exists())
            directory.mkdir();

        try {
            out = new FileOutputStream("file_" + fileId + "/chunk_" + chunkNo);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            int count = 0;

            for(int i = 0; i < msg.length - 1; i++) {

                if(count >= 2)
                    out.write((char) msg[i]);

                else if(msg[i] == 10)
                    count++;
            }

            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while(true) {
            byte[] msg = getPacketMessage(socket);;
            String message = new String(msg).replaceAll("\0", "");

            if (message != null) {
                String[] tokens = message.split(" ");
                if (Integer.parseInt(tokens[2]) != Peer.senderId && tokens[0].equals("PUTCHUNK")) {

                    createChunk(msg, tokens[3], tokens[4]);

                    String[] params = new String[]{tokens[3], tokens[4]};
                    message = MessageFactory.addHeader("STORED", params);
                    sendPacket(Mc.socket, message, Mc.address, Mc.port);
                }
            }
        }
    }
}
