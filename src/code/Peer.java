package code;

import channels.Mc;
import channels.Mdb;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Peer {
    static String version;
    public static int senderId;
    public static HashMap<String, StoreRequest> requests = new HashMap<>();
    public static HashMap<String, String> backedFiles = new HashMap<>();

    private static void setupThread(Thread th) {
        try {
            th.join();
        } catch (InterruptedException e) {
            System.err.println("Error waiting for thread");
            return;
        }
        th.start();
    }

    public static void main(String[] args) {
        if(args.length != 6)
            return;

        version = args[0];
        senderId = Integer.parseInt(args[1]);

        String mdbAddr = args[2];
        int mdbPort = Integer.parseInt(args[3]);

        String mcAddr = args[4];
        int mcPort = Integer.parseInt(args[5]);

        Thread mdb = new Thread(new Mdb(mdbAddr, mdbPort));
        Thread mc = new Thread(new Mc(mcAddr, mcPort));

        setupThread(mdb);
        setupThread(mc);

        backedFiles.put("image2.jpg","3");

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(20);

        if (senderId == 1) {
            /*int rd = 1;
            String file_path = "image2.jpg";

            StoreRequest req = new StoreRequest(executor, file_path, rd);
            requests.put("1", req);
            executor.schedule(req, 0, TimeUnit.SECONDS);*/

            String file_path = "image2.jpg";

            DeleteRequest req = new DeleteRequest(executor, backedFiles.get(file_path));
            executor.schedule(req, 0, TimeUnit.SECONDS);

            try {
                executor.awaitTermination(1, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            executor.shutdown();
        }
    }
}

