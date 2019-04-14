package mains;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PeerInterface extends Remote {
    void backup (String file_path, Integer replicationDegree) throws RemoteException;
    void restore (String file_path) throws RemoteException;
    void delete (String file_path) throws RemoteException;
    void reclaim (long maximum_space) throws  RemoteException;
    String state () throws RemoteException;
}
