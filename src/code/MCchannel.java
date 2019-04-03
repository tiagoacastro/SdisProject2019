package code;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class MCchannel implements Runnable{

    private InetAddress address;
    private int port;
    private int senderId;
    private int rd = 0;

    public MCchannel (String address, int port, int senderId){
        this.port = port;
        this.senderId = senderId;
        try {
            this.address = InetAddress.getByName(address);
            System.out.println("Multicast address created");
        }catch (UnknownHostException e){
                System.err.println("Multicast address unknown");
                System.exit(-2);
        }
    }

    @Override
    public void run() {

        MulticastSocket mcSocket = null;

        try{
            mcSocket = new MulticastSocket(this.port);
            mcSocket.joinGroup(this.address);
            mcSocket.setTimeToLive(1);
            System.out.println("Multicast socket set up successful");
        }catch(IOException e)
        {
            System.err.println("Error setting up multicast MC socket");
            System.exit(-1);
        }
        
        String message = null;
        while(rd != 0){
            try{
                byte[] msg = new byte[256];
                DatagramPacket packet = new DatagramPacket(msg, msg.length);

                System.out.println("wait");
                mcSocket.receive(packet);

                message = new String(packet.getData()).replaceAll("\0", "");

                System.out.println(new String(packet.getData(), 0, packet.getLength()));
            }catch(IOException e)
            {
                System.err.println("Error receiving packet");
                System.exit(-4);
            }
            
            if(message != null && checkId(message))
                rd--;
            
        }
    }

    private boolean checkId(String message){
        String[] tokens = message.split(" ");
        return Integer.parseInt(tokens[2]) != senderId;
    }

    public void setRD(int newValue) 
    {
        this.rd = newValue;
    }
}

