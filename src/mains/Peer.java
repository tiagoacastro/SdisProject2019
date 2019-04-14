package mains;

import Utilities.Auxiliary;
import Utilities.Key;
import Utilities.Value;
import requests.*;
import channels.Mc;
import channels.Mdb;
import channels.Mdr;

import java.io.*;
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
    private static ScheduledExecutorService executor;
    public static HashMap<String, StoreRequest> requests = new HashMap<>();
    public static HashMap<String, RestoreRequest> restoreRequests = new HashMap<>();
    public static HashMap<Key, Value> stores = new HashMap<>();
    public static HashMap<String, Integer> rds = new HashMap<>();
    public static ArrayList<String> sent = new ArrayList<>();
    public static long allowedSpace = 100000000;

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
                        rds.put(tokens[0], Integer.parseInt(tokens[1]));
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

    private static void loadStores(){
        File directoryPeer = new File("peer" + Peer.senderId);
        if (directoryPeer.exists()){
            File file = new File("peer" + Peer.senderId + "/stores.txt");
            if(file.exists()){
                try {
                    FileReader fr = new FileReader("peer" + Peer.senderId + "/stores.txt");
                    BufferedReader br = new BufferedReader(fr);
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] tokens = line.split(" ");
                        Key key = new Key(tokens[0], Integer.parseInt(tokens[1]));
                        Value value = new Value(Integer.parseInt(tokens[2]));
                        stores.put(key, value);
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

    private static void loadSpace(){
        File directoryPeer = new File("peer" + Peer.senderId);
        if (directoryPeer.exists()){
            File file = new File("peer" + Peer.senderId + "/space.txt");
            if(file.exists()){
                try {
                    FileReader fr = new FileReader("peer" + Peer.senderId + "/space.txt");
                    BufferedReader br = new BufferedReader(fr);
                    allowedSpace = Long.parseLong(br.readLine());
                    br.close();
                    fr.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        }
    }

    private static void loadSent(){
        File directoryPeer = new File("peer" + Peer.senderId);
        if (directoryPeer.exists()){
            File file = new File("peer" + Peer.senderId + "/sent.txt");
            if(file.exists()){
                try {
                    FileReader fr = new FileReader("peer" + Peer.senderId + "/sent.txt");
                    BufferedReader br = new BufferedReader(fr);
                    String line;
                    while ((line = br.readLine()) != null)
                        sent.add(line);
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

        for(Map.Entry<String, Integer> entry : rds.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            out.println(key + " " + value);
        }

        closeOutputStreams(fs, out);
    }

    private static void saveStores(){
        FileOutputStream fs = null;
        PrintWriter out = null;

        File directoryPeer = new File("peer" + Peer.senderId);
        if (!directoryPeer.exists())
            if(!directoryPeer.mkdir())
                return;

        try {
            fs = new FileOutputStream("peer" + Peer.senderId + "/stores.txt");
            out = new PrintWriter(fs);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        for(Map.Entry<Key, Value> entry : stores.entrySet()) {
            Key key = entry.getKey();
            Value value = entry.getValue();
            out.println(key + " " + value);
        }

        closeOutputStreams(fs, out);
    }

    private static void saveSpace(){
        FileOutputStream fs = null;
        PrintWriter out = null;

        File directoryPeer = new File("peer" + Peer.senderId);
        if (!directoryPeer.exists())
            if(!directoryPeer.mkdir())
                return;

        try {
            fs = new FileOutputStream("peer" + Peer.senderId + "/space.txt");
            out = new PrintWriter(fs);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        out.println(allowedSpace);

        closeOutputStreams(fs, out);
    }

    private static void saveSent(){
        FileOutputStream fs = null;
        PrintWriter out = null;

        File directoryPeer = new File("peer" + Peer.senderId);
        if (!directoryPeer.exists())
            if(!directoryPeer.mkdir())
                return;

        try {
            fs = new FileOutputStream("peer" + Peer.senderId + "/sent.txt");
            out = new PrintWriter(fs);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        for (String file : sent)
            out.println(file);

        closeOutputStreams(fs, out);
    }

    private static void closeOutputStreams(FileOutputStream fs, PrintWriter out) {
        try {
            out.close();
            fs.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static long getUsedSpace(){
        return Auxiliary.getDirectorySize(new File("peer" + Peer.senderId + "/backup"));
    }

    @Override
    public void backup(String file_path, Integer replicationDegree, boolean enhanced) {
        StoreRequest req = new StoreRequest(executor, file_path, replicationDegree);
        executor.submit(req);
    }

    @Override
    public void restore(String file_path, boolean enhanced) {
        RestoreRequest req = new RestoreRequest(file_path);
        executor.submit(req);
    }

    @Override
    public void delete(String file_path, boolean enhanced) {
        DeleteRequest req = new DeleteRequest(executor, file_path);
        executor.submit(req);
    }

    @Override
    public void reclaim(long maximum_space) {
        ReclaimRequest req = new ReclaimRequest(executor, maximum_space);
        executor.submit(req);
    }

    @Override
    public String state() {
        StateRequest req = new StateRequest();
        executor.submit(req);
        return req.getStateMessage();
    }

    private static class Hook extends Thread{
        @Override
        public void run() {
            saveSpace();
            saveRds();
            saveStores();
            saveSent();
        }
    }

    public static void main(String[] args) {
        if(args.length != 9)
            return;

        Runtime.getRuntime().addShutdownHook(new Hook());

        version = args[0];
        senderId = Integer.parseInt(args[1]);
        String accessPoint = args[2];

        try {
            Peer obj = new Peer();
            PeerInterface stub = (PeerInterface) UnicastRemoteObject.exportObject(obj, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(accessPoint, stub);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        loadSpace();
        loadRds();
        loadStores();
        loadSent();

        String mdbAddr = args[3];
        int mdbPort = Integer.parseInt(args[4]);

        String mcAddr = args[5];
        int mcPort = Integer.parseInt(args[6]);

        String mdrAddr = args[7];
        int mdrPort = Integer.parseInt(args[8]);

        executor = Executors.newScheduledThreadPool(100);

         executor.submit(new Mdb(mdbAddr, mdbPort));
         executor.submit(new Mc(mcAddr, mcPort));
         executor.submit(new Mdr(mdrAddr, mdrPort));
/*
        if (senderId == 1 || senderId == 5) {
            int rd;
            String file_path;
            if(senderId == 1){
                rd = 2;
                file_path = "rsc/image.jpg";
            } else {
                rd = 2;
                file_path = "rsc/image3.jpg";
            }

            StoreRequest req = new StoreRequest(executor, file_path, rd);
            executor.submit(req);

            DeleteRequest req = new DeleteRequest(executor, file_path);
            executor.submit(req);

            RestoreRequest req = new RestoreRequest(executor, file_path);
            executor.submit(req);

            ReclaimRequest req = new ReclaimRequest(executor, 400);
            executor.submit(req);

        }

        if(senderId == 2){
            ReclaimRequest req = new ReclaimRequest(executor, 100000);
            executor.submit(req);
        }
*/
        try {
            executor.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        executor.shutdown();
    }
}
