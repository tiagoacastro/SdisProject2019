package code;

import java.util.ArrayList;

public class Chunk {

    private int chunkNo;
    private String  fileId;
    private byte [] body;
    private ArrayList<Integer> peers;

    public Chunk(int chunkNo, String fileId, byte[] body)
    {
        this.chunkNo = chunkNo;
        this.fileId = fileId;
        this.body = body;
        this.peers = new ArrayList<>();
    }

    public int getNumber()
    {
        return this.chunkNo;
    }

    public String getFileId() { return this.fileId;}

    public byte[] getBody()
    {
        return this.body;
    }

    public void addPeer(int peerId) {
        this.peers.add(peerId);
    }

    public ArrayList<Integer> getPeers() {return this.peers;}
}
