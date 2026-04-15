package mattb.server;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader reader;
    public PrintWriter writer;
    private final String name;

    /**
     * Initializes {@link ClientHandler ClientHandler's} reader and writer
     *
     * @param socket {@link Socket} used for your connection
     * @param name   Name of user
     */
    public ClientHandler(Socket socket, String name) {
        this.name = name;
        try {
            this.socket = socket;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prints out whatever is recieved from the client to the servers console and also broadcasts it to all other clients
     * It also prints and broadcasts a message when a client disconnects
     */
    @Override
    public void run() {
        String message;
        try {
            while ((message = reader.readLine()) != null) {
                System.out.println(name + ": " + message);
                Server.broadcast(this, name + ": " + message);
            }
        } catch (IOException e) {
            System.out.println(name + " disconnected");
            Server.broadcast(this, name + " disconnected");
        } finally {
            closeEverything();
        }
    }

    /**
     * Shuts down the {@link ClientHandler ClientHandler's} {@link BufferedReader}, {@link PrintWriter}, and {@link Socket}
     */
    private void closeEverything() {
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
