import java.util.Arrays;
import java.util.List;

public class KeyValueClient {

    public static void main(String[] args) {
        System.setProperty("sun.rmi.transport.connectionTimeout", "5000");
        System.setProperty("sun.rmi.transport.tcp.responseTimeout", "5000");

        try {
            List<String> replicaUrls = Arrays.asList(
                    "rmi://localhost:5001/KeyValueService",
                    "rmi://localhost:5002/KeyValueService",
                    "rmi://localhost:5003/KeyValueService",
                    "rmi://localhost:5004/KeyValueService",
                    "rmi://localhost:5005/KeyValueService");

            KeyValueCoordinator coordinator = new KeyValueCoordinator(replicaUrls);
            ClientUtil.log("Checking replica health...");
            coordinator.checkReplicas();
            ClientUtil.log("Connected to Key-Value Server.");

            if (!coordinator.checkPrepopulated()) {
                String[] prepopulateCommands = {
                        "PUT name Jasmine",
                        "PUT age 28",
                        "PUT city San Jose",
                        "PUT country USA",
                        "PUT language English"
                };

                for (String command : prepopulateCommands) {
                    String response = sendRequest(coordinator, command);
                    ClientUtil.log("Prepopulated: " + command + " -> " + response);
                }

                coordinator.setPrepopulated();
                ClientUtil.log("Prepopulation complete.");
            } else {
                ClientUtil.log("Server is already prepopulated.");
            }

            while (true) {
                String command = ClientUtil.getUserInput();
                if (command.equalsIgnoreCase("exit")) {
                    ClientUtil.log("Exiting client...");
                    break;
                }

                if (!ClientUtil.isValidCommand(command)) {
                    ClientUtil.log("Invalid command. Use: PUT <key> <value>, GET <key>, DELETE <key>");
                    continue;
                }

                String response = sendRequest(coordinator, command);
                ClientUtil.log("Server Response: " + response);
            }
        } catch (Exception e) {
            ClientUtil.log("Client Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String sendRequest(KeyValueCoordinator coordinator, String command) throws Exception {
        Command c = CommandUtil.createCommand(command);
        return switch (c.getOperation()) {
            case "PUT" -> coordinator.put(c.getKey(), c.getValue());
            case "GET" -> coordinator.get(c.getKey());
            case "DELETE" -> coordinator.delete(c.getKey());
            default -> "Error: Unknown command";
        };
    }
}