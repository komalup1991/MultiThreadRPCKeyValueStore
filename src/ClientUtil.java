import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientUtil {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Logs a message with a timestamp.
     * 
     * @param message The message to log.
     */
    public static void log(String message) {
        System.out.println(dateFormat.format(new Date()) + " - " + message);
    }

    /**
     * Reads user input from the console.
     * 
     * @return The input string entered by the user, or an empty string in case of
     *         an error.
     */
    public static String getUserInput() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter commands(e.g. PUT name Jasmine, GET name, DELETE name)  ");
        try {
            return reader.readLine().trim();
        } catch (IOException e) {
            log("Error reading input: " + e.getMessage());
            return "";
        }
    }

    /**
     * Validates whether the given command follows the expected format.
     * Expected command format: "PUT <key> <value>", "GET <key>", or "DELETE <key>".
     * 
     * @param command The command string entered by the user.
     * @return true if the command is valid, false otherwise.
     */
    public static boolean isValidCommand(String command) {
        Command c = CommandUtil.createCommand(command);
        return c.toString().matches("^(PUT|GET|DELETE)\\s+\\w+(\\s+.+)?$");
    }
}