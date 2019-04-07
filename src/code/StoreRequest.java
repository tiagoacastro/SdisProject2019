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
    private ArrayList<Chunk> chunks = new ArrayList<>();

    StoreRequest(Timer t, String fp, int rd) {
        this.file = new File(fp);
        this.rd = rd;
        this.t = t;
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
        int count = 0, ignore = 0, sends = 0, messageSize;
        byte [] headerBytes, message;

        String[] params = new String[]{String.valueOf(chunk.getFileId()), String.valueOf(chunk.getNumber()), String.valueOf(rd)};
        String header = MessageFactory.addHeader("PUTCHUNK", params);

        headerBytes = header.getBytes();
        messageSize = headerBytes.length + chunk.getBody().length;
        message = new byte[messageSize];

        System.arraycopy(headerBytes, 0, message, 0, headerBytes.length);
        System.arraycopy(chunk.getBody(), 0, message, headerBytes.length, chunk.getBody().length);

        while(sends < 5) {
            if (count == ignore) {
                int numberPeers = chunks.get(chunk.getNumber()).getPeers().size();
                if (numberPeers < rd) {
                    System.out.println("rd not achieved");
                    Channel.sendPacketBytes(Mdb.socket, message, Mdb.address, Mdb.port);
                    sends++;
                    count = 0;
                    switch (sends) {
                        case 2:
                            ignore = 1;
                            break;
                        case 3:
                            ignore = 3;
                            break;
                        case 4:
                            ignore = 7;
                            break;
                    }

                } else {
                    System.out.println("rd achieved");
                    break;
                }
            } else
                count++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }

        if(sends == 5)
            System.out.println("Too many sends");
    }

    public void run() {
        getChunks();

        for(Chunk c: this.chunks)
        {
            System.out.println("Sending chunk #" + c.getNumber());
            sendChunk(c);
        }

        t.cancel();
    }
}
