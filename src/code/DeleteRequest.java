package code;

import channels.Channel;
import channels.Mc;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DeleteRequest implements Runnable {
    private ScheduledExecutorService executor;
    private String fileId;
    private boolean first = true;

    DeleteRequest(ScheduledExecutorService executor, String fp) {
        this.executor = executor;

        this.fileId = Auxiliary.encodeFileId(new File(fp));
    }

    @Override
    public void run() {
        if(first)
            for (Map.Entry<Key, Value> entry : Peer.stores.entrySet()) {
                Key k = entry.getKey();

                if (k.file.equals(fileId))
                    entry.getValue().stores = 0;
            }
        first = false;
        String[] params = new String[]{this.fileId};
        String message = Auxiliary.addHeader("DELETE", params);
        Channel.sendPacketBytes(Mc.socket, message.getBytes(), Mc.address, Mc.port);
        executor.schedule(this, 1, TimeUnit.SECONDS);
    }
}
