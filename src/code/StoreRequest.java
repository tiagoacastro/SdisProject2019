package code;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StoreRequest implements Runnable {
    private ScheduledExecutorService executor;
    private String file_path;
    private File file;
    private String fileId;
    private int rd;
    private ArrayList<Chunk> chunks = new ArrayList<>();

    StoreRequest(ScheduledExecutorService executor, String fp, int rd) {
        this.executor = executor;
        this.rd = rd;
        this.file_path = fp;
        this.file = new File(fp);

        this.fileId = Auxiliary.encodeFileId(file);
        Peer.requests.put(this.fileId, this);

        splitIntoChunks();
    }

    public void store(int chunkNo) {
        chunks.get(chunkNo).store();
    }

    private void splitIntoChunks() {
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

                int stores = 0;
                Value value = Peer.rds.get(new Key(fileId, chunkNo));
                if(value != null)
                    stores = value.stores;
                Chunk chunk = new Chunk(chunkNo, this.fileId, trimmedBuf, rd, executor, stores);
                this.chunks.add(chunk);

                chunkNo++;
            }

            if(file.length() % maxChunkSize == 0)
            {
                int stores = 0;
                Value value = Peer.rds.get(new Key(fileId, chunkNo));
                if(value != null)
                    stores = value.stores;
                Chunk chunk = new Chunk(chunkNo, this.fileId, null, rd, executor, stores);
                this.chunks.add(chunk);
            }
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public String getFile_path() {return this.file_path;}

    public int getRd() {return this.rd;}

    public ArrayList<Chunk> getChunks() {return this.chunks;}

    @Override
    public void run() {
        for(Chunk c : chunks)
            executor.schedule(c, 0, TimeUnit.SECONDS);
    }
}
