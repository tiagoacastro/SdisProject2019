package code;

import java.io.*;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StoreRequest implements Runnable {
    private ScheduledExecutorService executor;
    private File file;
    private String fileId;
    private int rd;
    private ArrayList<Chunk> chunks = new ArrayList<>();

    StoreRequest(ScheduledExecutorService executor, String fp, int rd) {
        this.executor = executor;
        this.rd = rd;
        this.file = new File(fp);

        this.fileId = Auxiliary.encodeFileId(file);
        Peer.requests.put(this.fileId, this);

        getChunks();
    }

    public void store(int chunkNo, int senderId) {
        chunks.get(chunkNo).addPeer(senderId);
    }

    private void getChunks() {
        int maxChunkSize = 64000, chunkNo = 0, bytesRead;
        byte[] buf = new byte[maxChunkSize];

        FileInputStream inputStream = null;

        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        try {
            while ((bytesRead = inputStream.read(buf)) > 0) {
                byte[] trimmedBuf  = Arrays.copyOf(buf, bytesRead);

                Chunk chunk = new Chunk(chunkNo, this.fileId, trimmedBuf, rd, executor);
                this.chunks.add(chunk);

                chunkNo++;
            }

            if(file.length() % maxChunkSize == 0)
            {
                Chunk chunk = new Chunk(chunkNo, this.fileId, null, rd, executor);
                this.chunks.add(chunk);
            }
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @Override
    public void run() {
        for(Chunk c : chunks)
            executor.schedule(c, 0, TimeUnit.SECONDS);
    }
}
