package code;

import channels.Channel;
import channels.Mdb;

import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Chunk implements Runnable{
    private ScheduledExecutorService executor;
    private int chunkNo;
    private String  fileId;
    private byte [] body;
    private ArrayList<Integer> peers;
    private int rd;
    private int sends = 0;

    public Chunk(int chunkNo, String fileId, byte[] body, int rd, ScheduledExecutorService executor)
    {
        this.chunkNo = chunkNo;
        this.fileId = fileId;
        this.body = body;
        this.peers = new ArrayList<>();
        this.rd = rd;
        this.executor = executor;
    }

    public void addPeer(int peerId) {
        this.peers.add(peerId);
    }

    @Override
    public void run(){
        int messageSize;
        byte [] headerBytes, message;

        if (peers.size() < rd) {
            String[] params = new String[]{String.valueOf(fileId), String.valueOf(chunkNo), String.valueOf(rd)};
            String header = MessageFactory.addHeader("PUTCHUNK", params);
            headerBytes = header.getBytes();
            messageSize = headerBytes.length + body.length;
            message = new byte[messageSize];
            System.arraycopy(headerBytes, 0, message, 0, headerBytes.length);
            System.arraycopy(body, 0, message, headerBytes.length, body.length);

            Channel.sendPacketBytes(Mdb.socket, message, Mdb.address, Mdb.port);
            sends++;
            if(sends == 5){
                System.out.println("too many sends #" + chunkNo);
            } else {
                switch (sends) {
                    case 2:
                        executor.schedule(this, 2, TimeUnit.SECONDS);
                        break;
                    case 3:
                        executor.schedule(this, 4, TimeUnit.SECONDS);
                        break;
                    case 4:
                        executor.schedule(this, 8, TimeUnit.SECONDS);
                        break;
                    default:
                        executor.schedule(this, 1, TimeUnit.SECONDS);
                        break;
                }
            }
        } else {
            System.out.println("rd achieved #" + chunkNo);
        }
    }
}
