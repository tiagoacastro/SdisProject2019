//package code;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MCchannel implements Runnable{

    private InetAddress address;
    private int port;
    private int senderId;

    public MCchannel (String address, int port, int senderId){
        this.port = port;
        this.senderId = senderId;
        this.address = InetAddress.getByName(address);
        System.out.println("Multicast address created");
    }

    @Override
    public void run() {

        MulticastSocket mcSocket = new MulticastSocket(port);
        mcSocket.joinGroup(address);
        mcSocket.setTimeToLive(1);
        System.out.println("Multicast socket set up successful");
        
        String message;
        while(rd != 0){
            byte[] msg = new byte[256];
            DatagramPacket packet = new DatagramPacket(msg, msg.length);

            System.out.println("wait");
            mcSocket.receive(packet);

            System.out.println(new String(packet.getData(), 0, packet.getLength()));

            if(message != null && check(message))
                rd--;
        }
    }

    private boolean checkId(String message){
        String[] tokens = message.split(" ");
        return Integer.parseInt(tokens[2]) != senderId;
    }
}

