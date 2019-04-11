package code;

import channels.Channel;
import channels.Mc;
import channels.Mdb;
import channels.Mdr;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Peer implements PeerInterface{
    public static String version;
    public static int senderId;
    public static String accessPoint;
    public static HashMap<String, StoreRequest> requests = new HashMap<>();
    public static HashMap<String, RestoreRequest> restoreRequests = new HashMap<>();
    public static HashMap<Key, Integer> rds = new HashMap<>();

    private static void setupThread(Thread th) {
        try {
            th.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        th.start();
    }

    private static void loadRds(){
        File directoryPeer = new File("peer" + Peer.senderId);
        if (directoryPeer.exists()){
            File file = new File("peer" + Peer.senderId + "/rds.txt");
            if(file.exists()){
                try {
                    FileReader fr = new FileReader("peer" + Peer.senderId + "/rds.txt");
                    BufferedReader br = new BufferedReader(fr);
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] tokens = line.split(" ");
                        Key key = new Key(tokens[0], Integer.parseInt(tokens[1]));
                        rds.put(key, Integer.parseInt(tokens[2]));
                    }
                    br.close();
                    fr.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        }
    }

    private static void saveRds(){
        FileOutputStream fs = null;
        PrintWriter out = null;

        File directoryPeer = new File("peer" + Peer.senderId);
        if (!directoryPeer.exists())
            if(!directoryPeer.mkdir())
                return;

        try {
            fs = new FileOutputStream("peer" + Peer.senderId + "/rds.txt");
            out = new PrintWriter(fs);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        for(Map.Entry<Key, Integer> entry : rds.entrySet()) {
            Key key = entry.getKey();
            Integer value = entry.getValue();

            out.println(key + " " + value);
        }

        try {
            out.close();
            fs.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @Override
    public void backup(String file_path, Integer replicationDegree) throws RemoteException {

    }

    @Override
    public void restore(String file_path) throws RemoteException {

    }

    @Override
    public void delete(String file_path) throws RemoteException {

    }

    @Override
    public void reclaim(int maximum_space) throws RemoteException {

    }

    @Override
    public void state() throws RemoteException {

    }

    private static class Hook extends Thread{
        @Override
        public void run() {
            saveRds();
        }
    }

    public static void main(String[] args) {
        if(args.length != 8)
            return;

        Runtime.getRuntime().addShutdownHook(new Hook());

        version = args[0];
        senderId = Integer.parseInt(args[1]);
        /*
        accessPoint = "Peer" + senderId;

        try {
            Peer obj = new Peer();
            PeerInterface stub = (PeerInterface) UnicastRemoteObject.exportObject(obj, 0);
            Registry registry = LocateRegistry.createRegistry(1923);
            registry.bind(accessPoint, stub);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        */
        loadRds();

        String mdbAddr = args[2];
        int mdbPort = Integer.parseInt(args[3]);

        String mcAddr = args[4];
        int mcPort = Integer.parseInt(args[5]);

        String mdrAddr = args[6];
        int mdrPort = Integer.parseInt(args[7]);

        Thread mdb = new Thread(new Mdb(mdbAddr, mdbPort));
        Thread mc = new Thread(new Mc(mcAddr, mcPort));
        Thread mdr = new Thread(new Mdr(mdrAddr, mdrPort));

        setupThread(mdb);
        setupThread(mc);
        setupThread(mdr);

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(20);

        if (senderId == 1 || senderId == 5) {
            int rd;
            String file_path;
            if(senderId == 1){
                rd = 2;
                file_path = "rsc/image2.jpg";
            } else {
                rd = 2;
                file_path = "rsc/image3.jpg";
            }

            StoreRequest req = new StoreRequest(executor, file_path, rd);
            executor.schedule(req, 0, TimeUnit.SECONDS);

            /*DeleteRequest req = new DeleteRequest(executor, file_path);
            executor.schedule(req, 0, TimeUnit.SECONDS);*/

            /*RestoreRequest req = new RestoreRequest(executor, file_path);
            executor.schedule(req, 0, TimeUnit.SECONDS);*/

            /*ReclaimNotice nt = new ReclaimNotice(file_path, 1);
            executor.schedule(nt, 0, TimeUnit.SECONDS);*/

            try {
                executor.awaitTermination(1, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(-1);
            }
            executor.shutdown();
        }
    }
}

