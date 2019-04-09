package code;

import channels.Channel;
import channels.Mc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ScheduledExecutorService;

public class RestoreRequest implements Runnable {

    private File file;
    private ScheduledExecutorService executor;
    private String fileId;
    private int lastChunkReceived = -1;

    RestoreRequest (ScheduledExecutorService executor, String fp) {
        this.executor = executor;
        this.file = new File(fp);

        encodeFileId();
        Peer.restoreRequests.put(this.fileId, this);
    }

    private void encodeFileId() {
        String originalString = null;
        MessageDigest md = null;
        StringBuffer result = new StringBuffer();

        try {
            originalString = this.file.getName() + "_" +
                    this.file.lastModified() + "_" +
                    Files.getOwner(this.file.toPath());
        } catch (IOException e) {
            System.err.println("Error retrieving file information");
            System.exit(-1);
        }

        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error getting instance of MessageDigest");
            System.exit(-2);
        }

        md.update(originalString.getBytes());

        for (byte byt : md.digest())
            result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));

        this.fileId = result.toString();
    }

    public byte[] receiveChunk(int chunkNo, byte[] body)
    {
        this.lastChunkReceived = chunkNo;
        return body;
    }

    @Override
    public void run() {

        long numberChunks = (this.file.length() / 64000) + 1;

        for(int i = 0; i < numberChunks; i++)
        {
            String[] params = new String[]{this.fileId, String.valueOf(i)};
            String message = MessageFactory.addHeader("GETCHUNK", params);
            Channel.sendPacketBytes(Mc.socket, message.getBytes(), Mc.address, Mc.port);

            while(lastChunkReceived != i){}
        }

        System.out.println("Done");
    }
}
