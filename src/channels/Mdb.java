package channels;

import code.MessageFactory;
import code.Peer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Random;

public class Mdb extends Channel{
    public static InetAddress address;
    public static int port;
    public static MulticastSocket socket;

    public Mdb (String addr, int port){
        Mdb.address = getAddress(addr);
        Mdb.port = port;
        Mdb.socket = getMCSocket(address, port);
    }

    private void createChunk(byte[] msg, String fileId, String chunkNo, int rd)
    {
        FileOutputStream out = null;

        File directoryPeer = new File("peer" + Peer.senderId);
        if (!directoryPeer.exists())
            if(!directoryPeer.mkdir())
                return;

        File directoryBackup = new File("peer" + Peer.senderId + "/backup");
        if (!directoryBackup.exists())
            if(!directoryBackup.mkdir())
                return;

        File directoryFile = new File("peer" + Peer.senderId + "/backup/" + fileId);
        if (!directoryFile.exists())
            if(!directoryFile.mkdir())
                return;

        try {
            out = new FileOutputStream("peer" + Peer.senderId + "/backup/" + fileId + "/chk" + chunkNo + ":" + rd);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
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
            System.exit(-1);
        }
    }

    @Override
    public void run() {
        while(true) {
            byte[] msg = getPacketMessage(socket);
            if(msg != null) {
                String message = new String(msg).replaceAll("\0", "");

                if (message != null) {
                    String[] tokens = message.split(" ");
                    if (Integer.parseInt(tokens[2]) != Peer.senderId && tokens[0].equals("PUTCHUNK")) {

                        createChunk(msg, tokens[3], tokens[4], Integer.parseInt(tokens[5]));

                        String[] params = new String[]{tokens[3], tokens[4]};
                        message = MessageFactory.addHeader("STORED", params);

                        Random rand = new Random();
                        int interval = rand.nextInt(401);
                        try {
                            Thread.sleep(interval);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            System.exit(-1);
                        }

                        sendPacket(Mc.socket, message, Mc.address, Mc.port);
                    }
                }
            }
        }
    }
}
