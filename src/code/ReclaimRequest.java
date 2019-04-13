package code;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReclaimRequest implements Runnable{
    private boolean clean = false;
    private ScheduledExecutorService executor;

    ReclaimRequest(ScheduledExecutorService executor, long maximumSpace) {
        this.executor = executor;
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
        } else{
            for(Map.Entry<Key, Value> entry : Peer.rds.entrySet()) {
                Value value = entry.getValue();

                if(value.stores > value.rd){
                    Key key = entry.getKey();

                    RemovedNotice not = new RemovedNotice(key.file,key.chunk);
                    executor.schedule(not , 0, TimeUnit.SECONDS);

                    if(Peer.getUsedSpace() <= Peer.allowedSpace)
                        return;
                }
            }
            for(Map.Entry<Key, Value> entry : Peer.rds.entrySet()) {
                Value value = entry.getValue();

                if(value.stores > 1){
                    Key key = entry.getKey();

                    RemovedNotice not = new RemovedNotice(key.file,key.chunk);
                    executor.schedule(not , 0, TimeUnit.SECONDS);

                    if(Peer.getUsedSpace() <= Peer.allowedSpace)
                        return;
                }
            }
        }
    }
}
