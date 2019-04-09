package channels;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Arrays;

public abstract class Channel implements Runnable{
    static InetAddress getAddress(String address) {
        try {
            return InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return null;
    }

    static MulticastSocket getMCSocket(InetAddress address, int port) {
        try {
            MulticastSocket mcSocket = new MulticastSocket(port);
            mcSocket.joinGroup(address);
            mcSocket.setTimeToLive(1);
            return mcSocket;
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return null;
    }

    static byte[] getPacketMessage(MulticastSocket socket) {
        try {
            byte[] msg = new byte[65000];
            DatagramPacket packet = new DatagramPacket(msg, msg.length);
            socket.receive(packet);
            return trimMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return null;
    }

    private static byte[] trimMessage(byte []msg) {
        int paddingZeros = 0, index = 64999;
        while(true)
        {
            if(msg[index] != 0)
                break;

            paddingZeros++;
            index--;
        }

        return Arrays.copyOf(msg, msg.length - paddingZeros);
    }

    static void sendPacket(MulticastSocket socket, String message, InetAddress address, int port) {
        DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length, address, port);
        auxiliar(socket, packet);
    }

    public static void sendPacketBytes(MulticastSocket socket, byte[] message, InetAddress address, int port) {
        DatagramPacket packet = new DatagramPacket(message, message.length, address, port);
        auxiliar(socket, packet);
    }

    private static void auxiliar(MulticastSocket socket, DatagramPacket packet) {
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
