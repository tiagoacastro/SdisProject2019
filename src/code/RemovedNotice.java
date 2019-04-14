package code;

import channels.Channel;
import channels.Mc;

import java.io.File;

public class RemovedNotice implements Runnable {
    private String fileId;
    private int chunkNo;
    private boolean delete = true;

    RemovedNotice(String fileId, int chunkNo) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }

    @Override
    public void run() {
        File dir = new File("peer" + Peer.senderId + "/backup/" + fileId);

        if(dir.exists() && dir.isDirectory()){
            File chunk = new File("peer" + Peer.senderId + "/backup/" + fileId + "/chk" + chunkNo);
            if(chunk.exists()){
                if(!chunk.delete()){
                    System.out.println("Error deleting file " + fileId + " #" + chunkNo);
                    System.exit(-1);
                }

                String[] list = dir.list();

                if(list != null && list.length == 0){
                    if(!dir.delete()){
                        System.out.println("Error deleting dir " + fileId);
                        System.exit(-1);
                    }

                    System.out.println("Deleted dir " + fileId);
                }

                System.out.println("Deleted file " + fileId + " #" + chunkNo + " - Used Space: " + Peer.getUsedSpace());

                Key key = new Key(fileId, chunkNo);
                Peer.stores.get(key).decrement();
            }
            else
                delete = false;
        } else
            delete = false;
        
        if(delete) {
            String[] params = new String[]{this.fileId, Integer.toString(this.chunkNo)};
            String message = Auxiliary.addHeader("REMOVED", params);
            Channel.sendPacketBytes(Mc.socket, message.getBytes(), Mc.address, Mc.port);
        }
    }
}
