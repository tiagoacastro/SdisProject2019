package code;

public class RestoredChunk{

    private int chunkNo;
    private byte[] body;


    RestoredChunk(int chunkNo, byte[] body)
    {
        this.chunkNo = chunkNo;
        this.body = body;
    }

    public byte[] getBody() {return body;}
}
