package code;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class Peer {
    //private static MCchannel MC;
    private static int mdbPort;
    private static int mcPort;
    private static int mdrPort;
    private static MulticastSocket mdbSocket;
    private static InetAddress mdbAddress;
    private static MulticastSocket mcSocket;
    private static InetAddress mcAddress;
    private static MulticastSocket mdrSocket;
    private static InetAddress mdrAddress;
    private static int senderId;
    private static String version;
    private static HashMap<String, Request> requests = new HashMap<>();

/*    private enum MessageType {
        PUTCHUNK, STORED
    }*/

    private static class Request extends TimerTask{
        private Timer t;
        private String file;
        private int rd;
        private int sends = 0;
        private int ignore = 0;
        private int count = 0;
        private ArrayList<ArrayList<Integer>> peers = new ArrayList<>();

        Request(Timer t, String file, int rd){
            this.file = file;
            this.rd = rd;
            this.t = t;
            peers.add(new ArrayList<>());
        }

        public void store(int chunkNo, int senderId){
            rd--;
            peers.get(chunkNo-1).add(senderId);
        }

        public void run() {
            if(count == ignore) {
                if (peers.get(0).size() < rd) {
                    System.out.println("rd not achieved");
                    String[] params = new String[]{file, "1", String.valueOf(rd), "CONTENT OF THE FILE"};
                    String message = addHeader("PUTCHUNK", params);
                    sendPacket(mdbSocket, message, mdbAddress, mdbPort);
                    sends++;
                    if (this.sends == 5) {
                        System.out.println("too many sends");
                        t.cancel();
                    } else {
                        count = 0;
                        switch(sends){
                            case 2: ignore = 1; break;
                            case 3: ignore = 3; break;
                            case 4: ignore = 7; break;
                        }
                    }
                } else {
                    System.out.println("rd achieved");
                    t.cancel();
                }
            } else
                count++;
        }
    }

    private static class Mdb implements Runnable{
        @Override
        public void run() {
            String message = getPacketMessage(mdbSocket);
            if(message != null){
                String[] tokens = message.split(" ");
                if(check(tokens) && tokens[0].equals("PUTCHUNK")) {
                    String[] params = new String[]{tokens[3], tokens[4]};
                    message = addHeader("STORED", params);
                    sendPacket(mcSocket, message, mcAddress, mcPort);
                }
            }
        }
    }

    private static class Mc implements Runnable{
        @Override
        public void run() {
            String message;
            message = getPacketMessage(mcSocket);
            if(message != null) {
                String[] tokens = message.split(" ");
                if (check(tokens) && tokens[0].equals("STORED")) {
                    Request req = requests.get(tokens[3]);
                    req.store(Integer.parseInt(tokens[4]), Integer.parseInt(tokens[2]));
                }
            }
        }
    }
/*
    private static class Mdr implements Runnable{
        @Override
        public void run() {
            String message;
            while(rd != 0){
                message = getPacketMessage(mcSocket);
                if(message != null && check(message))
                {
                    if(hasChunk())
                    {
                        String[] params = new String[]{"1"};
                        message = addHeader("CHUNK", params);
                        sendPacket(mdrSocket, message, mcAddress, mcPort);
                    }
                }
            }
        }
    }
*/
    private static String addHeader(String type, String[] params){
        String message = type + " " + 
                         version + " " + 
                         senderId + " " +
                        params[0];

        for(int i = 1; i < params.length; i++)
        {
            message += " " + params[i];

            if(i == params.length - 2)
                if(type.equals("PUTCHUNK") || type.equals("CHUNK"))
                    break;
        }

        message += " \r\n\r\n";

        if(type.equals("PUTCHUNK") || type.equals("CHUNK"))
            message += " " + params[params.length-1];

        return message;
    }

    private static boolean check(String[] tokens){
        return Integer.parseInt(tokens[2]) != senderId;
    }
/*
    private static boolean hasChunk() {
        // Checks if peer has desired chunk. 
        return true;
    }
*/
    private static String getPacketMessage(MulticastSocket socket){
        try {
            byte[] msg = new byte[256];
            DatagramPacket packet = new DatagramPacket(msg, msg.length);
            System.out.println("wait");
            socket.receive(packet);
            System.out.println(new String(packet.getData(), 0, packet.getLength()));
            System.out.println("Received packet");
            return new String(packet.getData()).replaceAll("\0", "");
        } catch(IOException e){
            System.err.println("Error receiving packet");
            System.exit(-4);
        }
        return null;
    }

    private static void sendPacket(MulticastSocket socket, String message, InetAddress address, int port){
        DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length, address, port);
        try {
            socket.send(packet);
            System.out.println("Multicast packet sent");
        } catch (IOException e) {
            System.err.println("Multicast packet send failed");
            System.exit(-3);
        }
    }

    private static InetAddress getAddress(String address){
        try {
            InetAddress add = InetAddress.getByName(address);
            System.out.println("Multicast address created");
            return add;
        } catch (UnknownHostException e){
            System.err.println("Multicast address unknown");
            System.exit(-2);
        }
        return null;
    }

    private static MulticastSocket getMCSocket(InetAddress address, int port){
        try {
            MulticastSocket mcSocket = new MulticastSocket(port);
            mcSocket.joinGroup(address);
            mcSocket.setTimeToLive(1);
            System.out.println("Multicast socket set up successful");
            return mcSocket;
        } catch (IOException e){
            System.err.println("Error setting up multicast MC socket");
            System.exit(-1);
        }
        return null;
    }

    private static void setupThread(Thread th) {
        try {
            th.join();
        } catch (InterruptedException e){
            System.err.println("Error waiting for thread");
            return;
        }
        th.start();
    }

    public static void main(String[] args) {
        if(args.length != 6)
            return;

        version = args[0];
        senderId = Integer.parseInt(args[1]);
        String mdbAddr = args[2];
        mdbPort = Integer.parseInt(args[3]);
        String mcAddr = args[4];
        mcPort = Integer.parseInt(args[5]);

//        String mdrAddr = args[6];
//        mdrPort = Integer.parseInt(args[7]);

        mdbAddress = getAddress(mdbAddr);
        mcAddress = getAddress(mcAddr);

//        mdrAddress = getAddress(mdrAddr);

        mdbSocket = getMCSocket(mdbAddress, mdbPort);
        mcSocket = getMCSocket(mcAddress, mcPort);

//        mdrSocket = getMCSocket(mdrAddress, mdrPort);

        Thread mdb = new Thread(new Mdb());
        Thread mc = new Thread(new Mc());

//        Thread mdr = new Thread(new Mdr());
        //Thread mc = new Thread(new MCchannel(mcAddr, mcPort, senderId));
        //MC = new MCchannel(mcAddr, mcPort, senderId);

        setupThread(mdb);
        setupThread(mc);

//        setupThread(mdr);

        if(senderId == 1){
            int rd = 1;
            String file = "files/file";

            //MC.setRD(1);

            Timer t = new Timer();
            Request req = new Request(t, file, rd);
            requests.put(file, req);
            t.scheduleAtFixedRate(req, 0, 1000);
        }
    }
}

