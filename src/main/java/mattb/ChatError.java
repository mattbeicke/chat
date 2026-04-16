package mattb;

/**
 * Used by {@link ChatException} to display custom exception messages
 *
 * @author Matthew Beicke
 */
public enum ChatError {
    // Client side
    CLIENT_SEND_FAILED("Send message to server failed"),
    CLIENT_RECEIVE_FAILED("Receive message from server failed"),
    CLIENT_CONNECTION_FAILED("Client socket failed to connect to server"),
    CLIENT_SHUTDOWN_FAILED("Client failed to shut down correctly"),

    // Server side
    SERVER_SEND_FAILED("Send message to client failed"),
    SERVER_RECEIVE_FAILED("Receive message from client failed"), // Unused
    SERVER_CONNECTION_FAILED("Server socket failed to connect to client"),
    SERVER_SHUTDOWN_FAILED("Server failed to shut down correctly"),
    SERVER_CLIENT_SHUTDOWN_FAILED("Server failed to shut down client socket"),

    // Misc
    UNKNOWN_MESSAGE_TYPE("Message type from server is unknown"),
    UNKNOWN_CLIENT_TYPE("Client type does not exist");

    private final String message;

    ChatError(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
