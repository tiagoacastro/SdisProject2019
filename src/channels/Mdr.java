package channels;

import code.Peer;
import code.RestoreRequest;

import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.HashMap;

public class Mdr extends Channel{
    public static InetAddress address;
    public static int port;
    public static MulticastSocket socket;
    private static HashMap<String, RestoreRequest> restoreRequests;

    public Mdr (String addr, int port, HashMap<String, RestoreRequest> requests){
        Mdr.address = getAddress(addr);
        Mdr.port = port;
        Mdr.socket = getMCSocket(address, port);
        Mdr.restoreRequests= requests;
    }

    private byte[] getBody (byte[] msg)
    {
        byte[] body = new byte[64000];
        int count = 0, index = 0;

        for(int i = 0; i < msg.length - 1; i++) {

            if(count >= 2)
            {
                body[index] = msg[i];
                index++;
            }

            else if(msg[i] == 10)
                count++;
        }

        return Arrays.copyOf(body, index-1);
    }

    @Override
    public void run() {
        while(true) {
            byte[] msg = getPacketMessage(socket);
            if(msg != null) {
                String message = new String(msg).replaceAll("\0", "");

                if (message != null) {
                    String[] tokens = message.split(" ");
                    if (Integer.parseInt(tokens[2]) != Peer.senderId && tokens[0].equals("CHUNK")) {

                            byte[] body;

                            RestoreRequest req = restoreRequests.get(tokens[3]);
                            body = getBody(msg);
                            req.receiveChunk(Integer.parseInt(tokens[4]), body);
                    }
                }
            }
        }
    }
}