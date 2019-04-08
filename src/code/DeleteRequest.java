package code;

import channels.Channel;
import channels.Mc;

import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DeleteRequest extends TimerTask {

    private ScheduledExecutorService executor;
    private String file_path;

    DeleteRequest(ScheduledExecutorService executor, String fp) {
        this.executor = executor;
        this.file_path =  fp;

        executor.schedule(this, 0, TimeUnit.SECONDS);
    }

    @Override
    public void run() {

        //TODO Determinar file_id de acordo com  file_path
        String[] params = new String[]{"1"};  //hardcoded
        String message = MessageFactory.addHeader("DELETE", params);
        Channel.sendPacketBytes(Mc.socket, message.getBytes(), Mc.address, Mc.port);


    }
}
