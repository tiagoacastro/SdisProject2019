package code;

import channels.Channel;
import channels.Mc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class RestoreRequest implements Runnable {

    private File file;
    private String file_path; 
    private String fileId;
    private ArrayList<byte[]> chunksContent = new ArrayList<>();

    RestoreRequest (String fp) {
        this.file_path = fp;
        this.file = new File(fp);

        this.fileId = Auxiliary.encodeFileId(file);
        Peer.restoreRequests.put(this.fileId, this);
    }

    public synchronized void receiveChunk(int chunkNo, byte[] body)
    {
        if(this.chunksContent.size() == chunkNo) {
            this.chunksContent.add(body);
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

        byte[] fpBytes = this.file_path.getBytes();
        String fileName = "";

        for(int i = 0; i < fpBytes.length; i++)
        {
            char c = (char) fpBytes[i];

            if(c == '/')
                fileName = "";

            else
                fileName += c;
        }

        try {
            out = new FileOutputStream("peer" + Peer.senderId + "/restored/" + fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        try {

            for(byte[] chunkBody : this.chunksContent) {

                for (byte b : chunkBody)
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
        int chunkNo = 0;

        do {
            String[] params = new String[]{this.fileId, String.valueOf(chunkNo)};
            String message = Auxiliary.addHeader("GETCHUNK", params);
            Channel.sendPacketBytes(Mc.socket, message.getBytes(), Mc.address, Mc.port);

            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            chunkNo++;

        } while (this.chunksContent.get(chunkNo - 1).length == 64000);

        createFile();
    }
}
