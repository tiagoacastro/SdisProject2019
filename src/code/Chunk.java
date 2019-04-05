package code;

public class Chunk {

    private int chunkNo;
    private int fileId;
    private byte [] body;

    public Chunk(int chunkNo, int fileId, byte[] body)
    {
        this.chunkNo = chunkNo;
        this.fileId = fileId;
        this.body = body;
    }

    public byte[] getBody()
    {
        return this.body;
    }
}
