import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerUtil {
    // Date formatter for logging timestamps
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Logs a message with a timestamp and log level.
     * 
     * @param level   The log level (INFO, WARNING, ERROR).
     * @param message The message to log.
     */
    public static void log(String level, String message) {
        System.out.println(dateFormat.format(new Date()) + " [" + level + "] " + message);
    }

    /**
     * Logs an error message along with an exception.
     * 
     * @param message The error message.
     * @param e       The exception.
     */
    public static void logError(String message, Exception e) {
        System.err.println(dateFormat.format(new Date()) + " [ERROR] " + message);
        e.printStackTrace();
    }
}