package code;

import channels.Mc;
import channels.Mdb;
import channels.Mdr;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Peer{
    public static String version;
    public static int senderId;
    public static HashMap<String, StoreRequest> requests = new HashMap<>();
    public static HashMap<String, RestoreRequest> restoreRequests = new HashMap<>();

    public static void main(String[] args) {
        if(args.length != 8)
            return;

        version = args[0];
        senderId = Integer.parseInt(args[1]);

        String mdbAddr = args[2];
        int mdbPort = Integer.parseInt(args[3]);

        String mcAddr = args[4];
        int mcPort = Integer.parseInt(args[5]);

        String mdrAddr = args[6];
        int mdrPort = Integer.parseInt(args[7]);

        Thread mdb = new Thread(new Mdb(mdbAddr, mdbPort));
        Thread mc = new Thread(new Mc(mcAddr, mcPort));
        Thread mdr = new Thread(new Mdr(mdrAddr, mdrPort, restoreRequests));

        setupThread(mdb);
        setupThread(mc);
        setupThread(mdr);

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(20);

        if (senderId == 1) {
            int rd = 1;
            String file_path = "rsc/image2.jpg";

            StoreRequest req = new StoreRequest(executor, file_path, rd);
            executor.schedule(req, 0, TimeUnit.SECONDS);

            /*DeleteRequest req = new DeleteRequest(executor, file_path);
            executor.schedule(req, 0, TimeUnit.SECONDS);*/

            /*RestoreRequest req = new RestoreRequest(executor, file_path);
            executor.schedule(req, 0, TimeUnit.SECONDS);*/

            try {
                executor.awaitTermination(1, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(-1);
            }
            executor.shutdown();
        }
    }

    private static void setupThread(Thread th) {
        try {
            th.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        th.start();
    }
}

