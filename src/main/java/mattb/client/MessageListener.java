package mattb.client;

import javafx.application.Platform;
import javafx.scene.control.Label;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * This class is used to handle receiving and displaying input from the Server
 *
 * @author Matthew Beicke
 */
public class MessageListener implements Runnable {
    private Socket socket; // Socket used for your connection
    private DataInputStream in; // The incoming data stream
    private Label chat; // GUI chat history Label

    private volatile boolean running = true; // Used to tell the MessageListener to shut down

    /**
     * Initializes the {@link DataInputStream} for {@link MessageListener this} that's used for the {@link ClientCLI CLI}
     *
     * @param socket Your connection {@link Socket}
     */
    public MessageListener(Socket socket) {
        try {
            this.socket = socket;
            this.socket.setSoTimeout(1000);
            this.in = new DataInputStream(socket.getInputStream());
            this.chat = null;
        } catch (IOException e) {
            System.out.println("Server went offline or other issue encountered");
        }
    }

    /**
     * Initializes the {@link DataInputStream} for {@link MessageListener this} that's used for the {@link ClientGUI GUI}
     *
     * @param socket Your connection {@link Socket}
     * @param chat   {@link Label} used to display chat history
     */
    public MessageListener(Socket socket, Label chat) {
        try {
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.chat = chat;
        } catch (IOException e) {
            System.out.println("Server went offline or other issue encountered");
        }
    }

    /**
     * Handles receiving of information from server and displaying it where it needs to
     */
    @Override
    public void run() {
        try {
            while (running) {
                try {
                    int dataType = in.readInt();
                    String message;

                    switch (dataType) {
                        case 0: // Server sends shutdown command
                            message = in.readUTF();
                            if (chat != null) {
                                Platform.runLater(() -> chat.setText(chat.getText() + "\n" + message));
                            } else {
                                System.out.println(message);
                            }
                            shutdown();
                            break;
                        case 1: // Server sends text to display
                            message = in.readUTF();
                            if (chat != null) {
                                Platform.runLater(() -> chat.setText(chat.getText() + "\n" + message));
                            } else {
                                System.out.println(message);
                            }
                            break;
                        case 2: // Server sends user list
                            message = in.readUTF();
                            if (chat != null) {
                                Platform.runLater(() -> chat.setText(chat.getText() + "\n\nUsers Online:\n" + message));
                            } else {
                                System.out.println("\nUsers Online:\n" + message);
                            }
                            break;
                        default:
                            throw new RuntimeException("Unknown message from server");
                    }
                } catch (
                        SocketTimeoutException ignored) { // This is used to gracefully exit the infinite loop when running goes false
                }
            }
        } catch (IOException e) {
            System.out.println("Server went offline or other issue encountered");
        } finally {
            closeEverything();
        }
        Platform.exit(); // Shut down GUI
        System.exit(0); // Shut down CLI
    }

    /**
     * Shuts down the {@link ClientCLI CLI}/{@link ClientGUI GUI} without causing a {@link java.net.SocketException}
     */
    public void shutdown() {
        running = false;
        if (chat != null) {
            Platform.runLater(() -> chat.setText(chat.getText() + "\n\nShutting down..."));
        } else {
            System.out.println("\nShutting down...");
        }
    }

    /**
     * Closes the {@link DataInputStream} and {@link Socket} for {@link MessageListener this}
     */
    private void closeEverything() {
        try {
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.out.println("Server went offline or other issue encountered");
        }
    }
}
