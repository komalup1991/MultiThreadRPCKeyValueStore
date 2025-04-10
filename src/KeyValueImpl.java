import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

public class KeyValueImpl extends UnicastRemoteObject implements KeyValueInterface {
    private static final long serialVersionUID = 1L;
    private static ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, TransactionState> transactions = new ConcurrentHashMap<>();
    private boolean isPrepopulated = false;

    protected KeyValueImpl() throws java.rmi.RemoteException {
        super();
        ServerUtil.log("INFO", "Key-Value Store initialized successfully.");
    }

    @Override
    public boolean ping() throws RemoteException {
        return true;
    }

    @Override
    public String put(String key, String value) throws RemoteException {
        store.put(key, value);
        logClientRequest("PUT", key, value);
        return "Success: Key " + key + " stored.";
    }

    @Override
    public String get(String key) throws RemoteException {
        String value = store.get(key);
        logClientRequest("GET", key, null);
        return (value != null) ? value : "Error: Key not found.";
    }

    @Override
    public String delete(String key) throws RemoteException {
        String removed = store.remove(key);
        logClientRequest("DELETE", key, null);
        return (removed != null) ? "Success: Key " + key + " deleted." : "Error: Key not found.";
    }

    public synchronized boolean preparePut(String key, String value) {
        transactions.put(key, new TransactionState(value));
        ServerUtil.log("INFO", "Transaction PREPARE PUT | Key: " + key + " | Value: " + value);
        return true;
    }

    public synchronized boolean prepareDelete(String key) {
        if (!store.containsKey(key)) {
            ServerUtil.log("WARNING", "Transaction PREPARE DELETE failed | Key: " + key + " does not exist.");
            return false;
        }
        transactions.put(key, new TransactionState(null));
        ServerUtil.log("INFO", "Transaction PREPARE DELETE | Key: " + key);
        return true;
    }

    public synchronized void commit(String key) {
        TransactionState tx = transactions.get(key);
        if (tx != null) {
            if (tx.value != null) {
                store.put(key, tx.value);
                ServerUtil.log("INFO", "Transaction COMMIT PUT | Key: " + key + " | Value: " + tx.value);
            } else {
                store.remove(key);
                ServerUtil.log("INFO", "Transaction COMMIT DELETE | Key: " + key);
            }
            transactions.remove(key);
        }
    }

    public synchronized void rollback(String key) {
        transactions.remove(key);
        ServerUtil.log("WARNING", "Transaction ROLLBACK | Key: " + key);
    }

    private void logClientRequest(String operation, String key, String value) {
        try {
            String clientHost = java.rmi.server.RemoteServer.getClientHost();
            String threadName = Thread.currentThread().getName();
            String logMessage = "[" + threadName + "] Client [" + clientHost + "] sent: " + operation + " | Key: "
                    + key;
            if (value != null)
                logMessage += " | Value: " + value;
            ServerUtil.log("INFO", logMessage);
        } catch (Exception e) {
            ServerUtil.log("ERROR", "Failed to retrieve client host: " + e.getMessage());
        }
    }

    @Override
    public boolean checkPrepopulated() throws RemoteException {
        return isPrepopulated;
    }

    @Override
    public void setPrepopulated() throws RemoteException {
        this.isPrepopulated = true;
    }

    private static class TransactionState {
        String value;

        TransactionState(String value) {
            this.value = value;
        }
    }
}