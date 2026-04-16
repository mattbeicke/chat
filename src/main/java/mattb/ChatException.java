package mattb;

/**
 * Used to display custom exceptions
 *
 * @author Matthew Beicke
 */
public class ChatException extends RuntimeException {
    private ChatError error;

    public ChatException(ChatError error) {
        super(error.getMessage());
        this.error = error;
    }

    public ChatError getError() {
        return error;
    }
}
