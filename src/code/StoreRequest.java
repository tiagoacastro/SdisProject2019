package code;

import channels.Channel;
import channels.Mdb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StoreRequest extends TimerTask {
    private ScheduledExecutorService executor;
    private File file;
    private String file_path;
    private String fileId;
    private int rd;
    private ArrayList<Chunk> chunks = new ArrayList<>();

    StoreRequest(ScheduledExecutorService executor, String fp, int rd) {
        this.executor = executor;
        this.rd = rd;
        this.file_path = fp;
        this.file = new File(fp);

        encodeFileId();

        getChunks();
    }

    public void store(int chunkNo, int senderId) {
        chunks.get(chunkNo).addPeer(senderId);
    }

    private void encodeFileId()
    {
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
        Peer.requests.put(this.fileId, this);
    }

    private void getChunks() {
        int maxChunkSize = 64000, chunkNo = 0, bytesRead;
        byte[] buf = new byte[maxChunkSize];

        File file = new File(this.file_path);
        FileInputStream inputStream = null;

        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            System.err.println("Error opening file");
            System.exit(-5);
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
            System.err.println("Error reading file");
            System.exit(-6);
        }
    }

    @Override
    public void run() {
        for(Chunk c : chunks)
            executor.schedule(c, 0, TimeUnit.SECONDS);
    }
}
