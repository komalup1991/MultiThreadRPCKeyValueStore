import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.*;

public class KeyValueCoordinator {
    private final List<String> replicaUrls;
    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    private static final int TIMEOUT_SECONDS = 3;

    public KeyValueCoordinator(List<String> replicaUrls) {
        this.replicaUrls = replicaUrls;
    }

    public boolean isReplicaAvailable(String url) {
        Future<Boolean> future = executor.submit(() -> {
            try {
                KeyValueInterface replica = (KeyValueInterface) Naming.lookup(url);
                return replica.ping();
            } catch (Exception e) {
                return false;
            }
        });

        try {
            return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            return false;
        }
    }

    public void checkReplicas() {
        for (String url : replicaUrls) {
            boolean available = isReplicaAvailable(url);
            System.out.println("Replica at " + url + " is " + (available ? "UP" : "DOWN"));
        }
    }

    public boolean checkPrepopulated() {
        for (String url : replicaUrls) {
            Future<Boolean> future = executor.submit(() -> {
                try {
                    KeyValueInterface replica = (KeyValueInterface) Naming.lookup(url);
                    return replica.checkPrepopulated();
                } catch (Exception e) {
                    return false;
                }
            });

            try {
                return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (Exception e) {
                System.out.println("Timeout or error checking prepopulated state on: " + url);
            }
        }
        return true;
    }

    public void setPrepopulated() {
        for (String url : replicaUrls) {
            Future<Void> future = executor.submit(() -> {
                try {
                    KeyValueInterface replica = (KeyValueInterface) Naming.lookup(url);
                    replica.setPrepopulated();
                } catch (Exception e) {
                    System.out.println("Failed to set prepopulation flag on: " + url);
                }
                return null;
            });

            try {
                future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                return;
            } catch (Exception e) {
                System.out.println("Timeout setting prepopulated state on: " + url);
            }
        }
    }

    public String put(String key, String value) {
        boolean allPrepared = executeWithTimeout(replica -> replica.preparePut(key, value));
        executeWithTimeout(replica -> {
            if (allPrepared) {
                replica.commit(key);
            } else {
                replica.rollback(key);
            }
            return true;
        });
        return allPrepared ? "PUT successful across replicas." : "PUT failed (rollback triggered).";
    }

    public String delete(String key) {
        boolean allPrepared = executeWithTimeout(replica -> replica.prepareDelete(key));
        executeWithTimeout(replica -> {
            if (allPrepared) {
                replica.commit(key);
            } else {
                replica.rollback(key);
            }
            return true;
        });
        return allPrepared ? "DELETE successful across replicas." : "DELETE failed (rollback triggered).";
    }

    public String get(String key) {
        for (String url : replicaUrls) {
            Future<String> future = executor.submit(() -> {
                try {
                    KeyValueInterface replica = (KeyValueInterface) Naming.lookup(url);
                    return replica.get(key);
                } catch (Exception e) {
                    return null;
                }
            });

            try {
                String result = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                if (result != null)
                    return result;
            } catch (Exception e) {
                System.out.println("Timeout or error retrieving key from: " + url);
            }
        }
        return "Error: No available replicas.";
    }

    private boolean executeWithTimeout(RemoteAction action) {
        boolean success = true;
        for (String url : replicaUrls) {
            Future<Boolean> future = executor.submit(() -> {
                try {
                    KeyValueInterface replica = (KeyValueInterface) Naming.lookup(url);
                    return action.execute(replica);
                } catch (Exception e) {
                    return false;
                }
            });

            try {
                if (!future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    success = false;
                    break;
                }
            } catch (Exception e) {
                success = false;
                System.out.println("Timeout or error executing action on: " + url);
            }
        }
        return success;
    }

    @FunctionalInterface
    private interface RemoteAction {
        boolean execute(KeyValueInterface replica) throws RemoteException;
    }
}
