package code;

import channels.Channel;
import channels.Mc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DeleteRequest extends TimerTask {
    private File file;
    private ScheduledExecutorService executor;
    private String fileId;

    DeleteRequest(ScheduledExecutorService executor, String fp) {
        this.executor = executor;
        this.file = new File(fp);

        encodeFileId();
    }

    private void encodeFileId()
    {
        String originalString = null;
        MessageDigest md = null;
        StringBuffer result = new StringBuffer();

        try {
            originalString = this.file.getName() + "_" +
                    this.file.lastModified() + "_" +
                    Files.getOwner(this.file.toPath());
        } catch (IOException e) {
            System.err.println("Error retrieving file information");
            System.exit(-1);
        }

        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error getting instance of MessageDigest");
            System.exit(-2);
        }

        md.update(originalString.getBytes());

        for (byte byt : md.digest())
            result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));

        this.fileId = result.toString();
    }

    @Override
    public void run() {
        String[] params = new String[]{this.fileId};
        String message = MessageFactory.addHeader("DELETE", params);
        Channel.sendPacketBytes(Mc.socket, message.getBytes(), Mc.address, Mc.port);
        executor.schedule(this, 1, TimeUnit.SECONDS);
    }
}
