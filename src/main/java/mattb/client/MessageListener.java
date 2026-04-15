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
            e.printStackTrace();
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
            e.printStackTrace();
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

                    switch (dataType) {
                        case 1:
                            String message = in.readUTF();
                            if (chat != null) {
                                Platform.runLater(() -> chat.setText(chat.getText() + "\n" + message));
                            } else {
                                System.out.println(message);
                            }
                            break;
                        case 2:
                            break;
                    }
                } catch (SocketTimeoutException ignored) {
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeEverything();
        }
    }

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
            e.printStackTrace();
        }
    }
}
