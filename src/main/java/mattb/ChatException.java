package mattb;

/**
 * Used to display custom exceptions
 *
 * @author Matthew Beicke
 */
public class ChatException extends RuntimeException {
    public ChatException(ChatError error) {
        super(error.getMessage());
    }
}
