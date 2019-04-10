package channels;

import code.Key;
import code.MessageFactory;
import code.Peer;
import code.StoreRequest;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Mc extends Channel{
    public static InetAddress address;
    public static int port;
    public static MulticastSocket socket;
    private static ArrayList<Integer> chunksReceived = new ArrayList<>();

    public Mc (String addr, int port){
        Mc.address = getAddress(addr);
        Mc.port = port;
        Mc.socket = getMCSocket(address, port);

        chunksReceived.add(4);
    }

    public static void addChunk(int chunkNo) {chunksReceived.add(chunkNo);}

    private byte[] retrieveChunk(String fileId, String chunkNo)
    {
        int bytesRead;
        byte[] buf = new byte[65000], trimmedBuf = new byte[65000];

        File file = new File("peer" + Peer.senderId + "/backup/" + fileId + "/chk" + chunkNo);
        FileInputStream inputStream;

        if(!file.isFile())
            return null;

        try {
            inputStream = new FileInputStream("peer" + Peer.senderId + "/backup/" + fileId + "/chk" + chunkNo);

            while ((bytesRead = inputStream.read(buf)) > 0)
                trimmedBuf = Arrays.copyOf(buf, bytesRead);

            inputStream.close();
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return trimmedBuf;
    }

    @Override
    public void run() {
        while(true) {
            byte[] msg = getPacketMessage(socket);
            if(msg != null) {
                String message = new String(msg).replaceAll("\0", "");

                if (message != null) {
                    String[] tokens = message.split(" ");
                    if(tokens[0].equals("STORED")){
                        int rd = 0;
                        Key key = new Key(tokens[3], Integer.parseInt(tokens[4]));
                        if(Peer.rds.containsKey(key))
                            rd = Peer.rds.get(key);
                        rd++;
                        Peer.rds.put(key, rd);
                    }
                    if (Integer.parseInt(tokens[2]) != Peer.senderId)
                        switch (tokens[0]) {
                            case "STORED":
                                StoreRequest req = Peer.requests.get(tokens[3]);
                                if(req != null)
                                    req.store(Integer.parseInt(tokens[4]), Integer.parseInt(tokens[2]));
                                break;
                            case "DELETE":
                                File directory = new File("peer" + Peer.senderId + "/backup/" + tokens[3]);
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
                            case "GETCHUNK":
                                String header;
                                int messageSize;
                                byte [] headerBytes, getChunkMessage, body;

                                if((body = retrieveChunk(tokens[3], tokens[4])) != null)
                                {
                                    String[] params = new String[]{tokens[3], tokens[4]};
                                    header = MessageFactory.addHeader("CHUNK", params);
                                    headerBytes = header.getBytes();
                                    messageSize = headerBytes.length + body.length;

                                    getChunkMessage = new byte[messageSize];
                                    System.arraycopy(headerBytes, 0, getChunkMessage, 0, headerBytes.length);
                                    System.arraycopy(body, 0, getChunkMessage, headerBytes.length, body.length);

                                    Random rand = new Random();
                                    int interval = rand.nextInt(401);
                                    try {
                                        Thread.sleep(interval);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                        System.exit(-1);
                                    }

                                    if(!chunksReceived.contains(Integer.parseInt(tokens[4])))
                                        Channel.sendPacketBytes(Mdr.socket, getChunkMessage, Mdr.address, Mdr.port);
                                }
                                break;
                        }
                }
            }
        }
    }
}

