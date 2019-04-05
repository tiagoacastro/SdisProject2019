package code;

public class Chunk {

    private int chunkNo;
    private String  fileId;
    private byte [] body;

    public Chunk(int chunkNo, String fileId, byte[] body)
    {
        this.chunkNo = chunkNo;
        this.fileId = fileId;
        this.body = body;
    }

    public int getNumber()
    {
        return this.chunkNo;
    }

    public String getFileId()
    {
        return this.fileId;
    }

    public byte[] getBody()
    {
        return this.body;
    }
}
