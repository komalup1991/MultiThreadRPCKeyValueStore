public class Command {
    private final String operation;
    private final String key;
    private final String value;

    /**
     * Constructs a Command object with the specified operation, key, and value.
     *
     * @param operation The operation to be performed.
     * @param key       The key for the operation.
     * @param value     The value associated with the key (can be null for GET and
     *                  DELETE operations).
     */
    public Command(String operation, String key, String value) {
        this.operation = operation;
        this.key = key;
        this.value = value;
    }

    public String getOperation() {
        return operation;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return operation + " " + key + " " + value;
    }
}