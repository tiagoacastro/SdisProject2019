package code;

import channels.Channel;
import channels.Mc;

import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DeleteRequest extends TimerTask {

    private ScheduledExecutorService executor;
    private String file_id;

    DeleteRequest(ScheduledExecutorService executor, String fid) {
        this.executor = executor;
        this.file_id =  fid;

        executor.schedule(this, 0, TimeUnit.SECONDS);
    }

    @Override
    public void run() {

        String[] params = new String[]{this.file_id};
        String message = MessageFactory.addHeader("DELETE", params);
        Channel.sendPacketBytes(Mc.socket, message.getBytes(), Mc.address, Mc.port);
    }
}
