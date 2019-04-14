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
            Peer.allowedSpace = maximumSpace * 1000;
        else
            this.clean = true;
    }

    private void clearDirectory(File directory) {
        if (directory.exists()) {
            try {
                String[] files = directory.list();
                if(files != null){
                    for (String s : files) {
                        File currentFile = new File(directory.getPath(), s);
                        RemovedNotice not = new RemovedNotice(directory.getName(), Integer.parseInt(currentFile.getName().substring(3)));
                        executor.schedule(not , 0, TimeUnit.SECONDS);
                    }
                    if (!directory.delete())
                        throw new Exception("couldn't delete directory");
                }
            } catch(Exception e){
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }

    @Override
    public void run() {
        System.out.println("Used space: " + Peer.getUsedSpace());

        if(clean){
            deleteAllFiles();
        } else{
            deleteNeededFiles();
        }

        System.out.println("Used space: " + Peer.getUsedSpace());
    }

    private void deleteAllFiles() {
        File directory = new File("peer" + Peer.senderId + "/backup");
        String[] dirs = directory.list();
        if(dirs != null)
            for(String s : dirs){
                File dir = new File(directory.getPath(), s);
                clearDirectory(dir);
            }
        try {
            if (!directory.delete())
                throw new Exception("couldn't delete backup directory");
        } catch(Exception e){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void deleteNeededFiles() {
        boolean next = true;
        for(Map.Entry<Key, Value> entry : Peer.stores.entrySet()) {
            Value value = entry.getValue();
            Key key = entry.getKey();

            if(value.stores > Peer.rds.get(key.file)){
                RemovedNotice not = new RemovedNotice(key.file,key.chunk);
                executor.schedule(not , 0, TimeUnit.SECONDS);

                if(Peer.getUsedSpace() <= Peer.allowedSpace){
                    next = false;
                    break;
                }
            }
        }
        if(next){
            for(Map.Entry<Key, Value> entry : Peer.stores.entrySet()) {
                Value value = entry.getValue();

                if(value.stores > 1){
                    Key key = entry.getKey();

                    RemovedNotice not = new RemovedNotice(key.file,key.chunk);
                    executor.schedule(not , 0, TimeUnit.SECONDS);

                    if(Peer.getUsedSpace() <= Peer.allowedSpace){
                        next = false;
                        break;
                    }
                }
            }
            if(next)
                for(Map.Entry<Key, Value> entry : Peer.stores.entrySet()) {
                    Value value = entry.getValue();

                    if(value.stores != 0) {
                        Key key = entry.getKey();
                        RemovedNotice not = new RemovedNotice(key.file, key.chunk);
                        executor.schedule(not, 0, TimeUnit.SECONDS);

                        if (Peer.getUsedSpace() <= Peer.allowedSpace)
                            break;
                    }
                }
        }
    }
}
