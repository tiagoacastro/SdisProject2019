package code;

import channels.Channel;
import channels.Mdb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class StoreRequest extends TimerTask {
    private Timer t;
    private File file;
    private int rd;
    private int chunk = 0;
    private int count = 0;
    private int ignore = 0;
    private int sends = 0;
    private ArrayList<Chunk> chunks = new ArrayList<>();

    StoreRequest(Timer t, String fp, int rd) {
        this.file = new File(fp);
        this.rd = rd;
        this.t = t;

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
            System.err.println("Error opening file");
            System.exit(-5);
        }

        try {
            while ((bytesRead = inputStream.read(buf)) > 0) {
                byte[] trimmedBuf  = Arrays.copyOf(buf, bytesRead);

                Chunk chunk = new Chunk(chunkNo, "1", trimmedBuf);
                this.chunks.add(chunk);

                chunkNo++;
            }

            if(this.file.length() % maxChunkSize == 0)
            {
                Chunk chunk = new Chunk(chunkNo, "1", null);
                this.chunks.add(chunk);
            }

        }catch(IOException e)
        {
            System.err.println("Error reading file");
            System.exit(-6);
        }

    }

    private void sendChunk(Chunk chunk)
    {
        int messageSize;
        byte [] headerBytes, message;

        if (chunks.get(this.chunk).getPeers().size() < rd) {
            System.out.println("rd not achieved");

            String[] params = new String[]{String.valueOf(chunk.getFileId()), String.valueOf(chunk.getNumber()), String.valueOf(rd)};
            String header = MessageFactory.addHeader("PUTCHUNK", params);
            headerBytes = header.getBytes();
            messageSize = headerBytes.length + chunk.getBody().length;
            message = new byte[messageSize];
            System.arraycopy(headerBytes, 0, message, 0, headerBytes.length);
            System.arraycopy(chunk.getBody(), 0, message, headerBytes.length, chunk.getBody().length);

            Channel.sendPacketBytes(Mdb.socket, message, Mdb.address, Mdb.port);
            sends++;
            if(sends == 5){
                System.out.println("too many sends");
                t.cancel();
            } else {
                count = 0;
                switch (sends) {
                    case 2: ignore = 1; break;
                    case 3: ignore = 3; break;
                    case 4: ignore = 7; break;
                }
            }
        } else {
            System.out.println("rd achieved");
            this.chunk++;
            ignore = 0;
            count = 0;
            sends = 0;
        }
    }

    @Override
    public void run() {
        if(chunk == chunks.size())
            t.cancel();

        if (count == ignore) {
            Chunk c = chunks.get(chunk);
            System.out.println("Sending chunk #" + c.getNumber());
            sendChunk(c);
        } else
            count++;
    }
}
