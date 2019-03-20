package code;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

public class Peer {
    private static final String chunk = "efipkf ojwm wjrwjwrj";             //hardcoded
    private static final String store = "store";                            //hardcoded
    private static int mdbPort;
    private static int mcPort;
    private static MulticastSocket mdbSocket;
    private static InetAddress mdbAddress;
    private static MulticastSocket mcSocket;
    private static InetAddress mcAddress;
    private static int rd = 0;                                              //hardcoded
    private static int serverId;
    private static String version;

    private static class Mdb implements Runnable{
        @Override
        public void run() {
            String message = getPacketMessage(mdbSocket);
            if(message != null && check(message))
                sendPacket(mcSocket, store, mcAddress, mcPort);
        }
    }

    private static class Mc implements Runnable{
        @Override
        public void run() {
            String message;
            while(rd != 0){
                message = getPacketMessage(mcSocket);
                if(message != null && check(message))
                    rd--;
            }
        }
    }

    private static class Task extends TimerTask {
        public void run() {
            sendPacket(mdbSocket, chunk, mdbAddress, mdbPort);
        }
    }

    private static boolean check(String message){
        String[] tokens = message.split(" ");
        return Integer.parseInt(tokens[2]) == serverId;
    }

    private static String getPacketMessage(MulticastSocket socket){
        try {
            byte[] msg = new byte[256];
            DatagramPacket packet = new DatagramPacket(msg, msg.length);
            System.out.println("wait");
            socket.receive(packet);
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
        serverId = Integer.parseInt(args[1]);
        String mdbAddr = args[2];
        mdbPort = Integer.parseInt(args[3]);
        String mcAddr = args[4];
        mcPort = Integer.parseInt(args[5]);

        mdbAddress = getAddress(mdbAddr);
        mcAddress = getAddress(mcAddr);
        mdbSocket = getMCSocket(mdbAddress, mdbPort);
        mcSocket = getMCSocket(mcAddress, mcPort);

        Thread mdb = new Thread(new Mdb());
        Thread mc = new Thread(new Mc());
        setupThread(mdb);
        setupThread(mc);

        if(serverId == 1){
            rd = 1;
            Timer t = new Timer();
            t.scheduleAtFixedRate(new Task(), 0, 1000);
            while(rd != 0){
                System.out.print("");
            }
            t.cancel();
        }
    }
}

