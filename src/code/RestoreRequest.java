package code;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

        this.fileId = Auxiliary.encodeFileId(file);
        Peer.restoreRequests.put(this.fileId, this);
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

            for(RestoreChunk chunk : this.chunks) {

                byte[] body = chunk.getBody();

                for (byte b : body)
                    out.write((char) b);
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
            RestoreChunk rc = new RestoreChunk(i, this.fileId);
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
