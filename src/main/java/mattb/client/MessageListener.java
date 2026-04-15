package mattb.client;

import javafx.application.Platform;
import javafx.scene.control.Label;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class MessageListener implements Runnable {
    private Socket socket;
    private DataInputStream in;
    private Label chat;
    private volatile boolean running = true;

    /**
     * Initializes {@link MessageListener MessageListener's} reader for CLI
     *
     * @param socket {@link Socket} used for your connection
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
     * Initializes {@link MessageListener MessageListener's} reader for GUI
     *
     * @param socket {@link Socket} used for your connection
     * @param chat   {@link Label} used to display chat
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
     * Prints out whatever is recieved from the server
     */
    @Override
    public void run() {
        try {
            while (running) {
                try {
                    int dataType = in.readInt();
                    String message;

                    switch (dataType) {
                        case 0:
                            shutdown();
                            break;
                        case 1:
                            message = in.readUTF();
                            if (chat != null) {
                                Platform.runLater(() -> chat.setText(chat.getText() + "\n" + message));
                            } else {
                                System.out.println(message);
                            }
                            break;
                        case 2:
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
                } catch (SocketTimeoutException ignored) {
                }
            }
        } catch (IOException e) {
            System.out.println("Server went offline or other issue encountered");
        } finally {
            closeEverything();
        }
    }

    /**
     * shuts down the CLI/GUI without causing a {@link java.net.SocketException}
     */
    public void shutdown() {
        running = false;
    }

    /**
     * Shuts down the {@link MessageListener MessageListener's} {@link DataInputStream} and {@link Socket}
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
