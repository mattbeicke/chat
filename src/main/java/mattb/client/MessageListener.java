package mattb.client;

import javafx.application.Platform;
import javafx.scene.control.Label;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class MessageListener implements Runnable {
    private Socket socket;
    private BufferedReader reader;
    private Label chat;

    /**
     * Initializes {@link MessageListener MessageListener's} reader for CLI
     *
     * @param socket {@link Socket} used for your connection
     */
    public MessageListener(Socket socket) {
        try {
            this.socket = socket;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
        String message;
        try {
            while (!socket.isClosed() && (message = reader.readLine()) != null) {
                if (chat != null) {
                    String finalMessage = message;
                    Platform.runLater(() -> {
                        chat.setText(chat.getText() + "\n" + finalMessage);
                    });
                } else {
                    System.out.println(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeEverything();
        }
    }

    /**
     * Shuts down the {@link MessageListener MessageListener's} {@link BufferedReader} and {@link Socket}
     */
    private void closeEverything() {
        try {
            if (reader != null) reader.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
