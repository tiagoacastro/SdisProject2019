package code;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RestoreRequest implements Runnable {

    private File file;
    private ScheduledExecutorService executor;
    private String fileId;
    private int chunksReceived = 0;
    private ArrayList<RestoreChunk> chunks = new ArrayList<>();
    private long numberChunks;

    RestoreRequest (ScheduledExecutorService executor, String fp) {
        this.executor = executor;
        this.file = new File(fp);

        encodeFileId();
        Peer.restoreRequests.put(this.fileId, this);
    }

    private void encodeFileId() {
        String originalString = null;
        MessageDigest md = null;
        StringBuilder result = new StringBuilder();

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

    public synchronized void receiveChunk(int chunkNo, byte[] body)
    {
        if(this.chunks.get(chunkNo).getBody() == null) {
            this.chunks.get(chunkNo).addBody(body);
            this.chunksReceived++;

            if(this.chunksReceived == this.numberChunks)
                notifyAll();
        }

    }

    private void createFile()
    {
        FileOutputStream out = null;

        File directoryPeer = new File("peer" + Peer.senderId);
        if (!directoryPeer.exists())
            if(!directoryPeer.mkdir())
                return;

        File directoryRestore = new File("peer" + Peer.senderId + "/restored");
        if (!directoryRestore.exists())
            if(!directoryRestore.mkdir())
                return;

        try {
            out = new FileOutputStream("peer" + Peer.senderId + "/restored/" + fileId );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        try {

            for(int i = 0; i < this.chunks.size(); i++) {

                byte[] body = this.chunks.get(i).getBody();

                for (int j = 0; j < body.length - 1; j++)
                    out.write((char) body[j]);
            }

            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @Override
    public synchronized void run() {

        numberChunks = (this.file.length() / 64000) + 1;

        for(int i = 0; i < this.numberChunks; i++)
        {
            RestoreChunk rc = new RestoreChunk(i, this.fileId, this.executor);
            this.chunks.add(rc);
            this.executor.schedule(rc, 0, TimeUnit.SECONDS);
        }

        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        createFile();
    }
}
