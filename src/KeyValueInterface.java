import java.rmi.Remote;
import java.rmi.RemoteException;

public interface KeyValueInterface extends Remote {
    String put(String key, String value) throws RemoteException;

    String get(String key) throws RemoteException;

    String delete(String key) throws RemoteException;

    boolean checkPrepopulated() throws RemoteException;

    void setPrepopulated() throws RemoteException;

    boolean preparePut(String key, String value) throws RemoteException;

    boolean prepareDelete(String key) throws RemoteException;

    void commit(String key) throws RemoteException;

    void rollback(String key) throws RemoteException;

    boolean ping() throws RemoteException;
}
