import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class KeyValueServer {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java KeyValueServer <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);

        try {
            LocateRegistry.createRegistry(port);
            KeyValueInterface server = new KeyValueImpl();
            Naming.rebind("rmi://localhost:" + port + "/KeyValueService", server);
            System.out.println("Key-Value Server running on port " + port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}