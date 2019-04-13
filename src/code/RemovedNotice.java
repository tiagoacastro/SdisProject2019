package code;

import channels.Channel;
import channels.Mc;

import java.io.File;

public class RemovedNotice implements Runnable {
    private String fileId;
    private int chunkNo;

    RemovedNotice(String fileId, int chunkNo) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;

        File chunk = new File("peer" + Peer.senderId + "/backup/" + fileId + "/chk" + chunkNo);
        if(!chunk.delete()){
            System.out.println("Error deleting file");
            System.exit(-1);
        }

        System.out.println("Deleted file " + fileId + " #" + chunkNo + " - Used Space: " + Peer.getUsedSpace());

        Key key = new Key(fileId, chunkNo);
        Peer.stores.get(key).decrement();
    }

    @Override
    public void run() {
        String[] params = new String[]{this.fileId, Integer.toString(this.chunkNo)};
        String message = Auxiliary.addHeader("REMOVED", params);
        Channel.sendPacketBytes(Mc.socket, message.getBytes(), Mc.address, Mc.port);
    }
}
