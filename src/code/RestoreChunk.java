package code;

import channels.Channel;
import channels.Mc;

import java.util.concurrent.ScheduledExecutorService;

public class RestoreChunk implements Runnable{

    private ScheduledExecutorService executor;
    private String fileId;
    private int chunkNo;
    private byte[] body = null;


    public RestoreChunk(int chunkNo, String fileId, ScheduledExecutorService executor)
    {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.executor = executor;
    }

    public void addBody(byte[] body) {this.body = body;}

    public byte[] getBody() {return body;}

    @Override
    public void run() {
        String[] params = new String[]{this.fileId, String.valueOf(this.chunkNo)};
        String message = MessageFactory.addHeader("GETCHUNK", params);
        Channel.sendPacketBytes(Mc.socket, message.getBytes(), Mc.address, Mc.port);
    }
}
