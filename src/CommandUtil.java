public class CommandUtil {
    /**
     * Creates a Command object from a given command string.
     *
     * @param command The command string in the format "OPERATION key [value]".
     *                The value is optional and can be null for operations like GET
     *                and DELETE.
     * @return A Command object if the command is well-formed; otherwise, returns
     *         null.
     */
    public static Command createCommand(String command) {
        String[] parts = command.split(" ", 3);
        if (parts.length < 2) {
            System.out.println("Error: Malformed command");
            return null;
        }
        String operation = parts[0].toUpperCase();
        return new Command(operation, parts[1], (parts.length == 3) ? parts[2] : null);
    }
}