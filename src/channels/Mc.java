package channels;

import code.MessageFactory;
import code.Peer;
import code.StoreRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
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

    private byte[] retrieveChunk(String fileId, String chunkNo)
    {
        int bytesRead;
        byte[] buf = new byte[65000], trimmedBuf = new byte[65000];

        File file = new File("Files/" + fileId + "/" + chunkNo);
        FileInputStream inputStream = null;

        if(!file.isFile())
            return null;

        try {
            inputStream = new FileInputStream("Files/" + fileId + "/" + chunkNo);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            while ((bytesRead = inputStream.read(buf)) > 0) {
                trimmedBuf = Arrays.copyOf(buf, bytesRead);
            }
        } catch(IOException e) {
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
                    if (Integer.parseInt(tokens[2]) != Peer.senderId)
                        switch (tokens[0]) {
                            case "STORED":
                                StoreRequest req = requests.get(tokens[3]);
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
                                System.out.println("Recebeu mensagem2");
                                String header;
                                int messageSize;
                                byte [] headerBytes, getChunkMessage, body;

                                if((body = retrieveChunk(tokens[3], tokens[4])) != null)
                                {
                                    System.out.println("Tem chunk");
                                    String[] params = new String[]{tokens[3], tokens[4]};
                                    header = MessageFactory.addHeader("CHUNK", params);
                                    headerBytes = header.getBytes();
                                    messageSize = headerBytes.length + body.length;

                                    getChunkMessage = new byte[messageSize];
                                    System.arraycopy(headerBytes, 0, getChunkMessage, 0, headerBytes.length);
                                    System.arraycopy(body, 0, getChunkMessage, headerBytes.length, body.length);

                                    System.out.println("Enviado chunk");
                                    Channel.sendPacketBytes(Mdr.socket, getChunkMessage, Mdr.address, Mdr.port);
                                }
                                break;
                        }
                }
            }
        }
    }
}

