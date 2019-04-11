package code;

import channels.Channel;
import channels.Mc;

import java.io.File;

public class ReclaimNotice implements Runnable {
    private String fileId;
    private int chunkNo;

    ReclaimNotice(String fp, int chunkNo) {
        this.fileId = Auxiliary.encodeFileId(new File(fp));
        this.chunkNo = chunkNo;

        File chunk = new File("peer" + Peer.senderId + "/backup/" + fileId);
        if(!chunk.delete()){
            System.out.println("Error deleting file");
            System.exit(-1);
        }

        Key key = new Key(fileId, chunkNo);
        int rd = Peer.rds.get(key);
        rd--;
        Peer.rds.put(key, rd);
    }

    @Override
    public void run() {
        String[] params = new String[]{this.fileId, Integer.toString(this.chunkNo)};
        String message = Auxiliary.addHeader("REMOVED", params);
        Channel.sendPacketBytes(Mc.socket, message.getBytes(), Mc.address, Mc.port);
    }
}
