package code;

import java.io.File;

public class ReclaimRequest implements Runnable{
    private boolean clean = false;

    ReclaimRequest(long maximumSpace) {
        if(maximumSpace != 0)
            Peer.allowedSpace = maximumSpace;
        else
            this.clean = true;
    }

    @Override
    public void run() {
        if(clean){
            File directory = new File("peer" + Peer.senderId + "/backup");
            Auxiliary.clearDirectory(directory);
        } else {
            while(Peer.usedSpace > Peer.allowedSpace){
                //DELETE CHUNKS
            }
        }
    }
}
