package code;

import channels.Channel;
import channels.Mc;

public class RestoreChunk implements Runnable{

    private String fileId;
    private int chunkNo;
    private byte[] body = null;


    public RestoreChunk(int chunkNo, String fileId)
    {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
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
