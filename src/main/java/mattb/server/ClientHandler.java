package mattb.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    enum userType {
        CLI, GUI
    }

    private Socket socket;
    private BufferedReader reader;
    public PrintWriter writer;
    private final String name;
    private final userType type;

    /**
     * Initializes {@link ClientHandler ClientHandler's} reader and writer
     *
     * @param socket {@link Socket} used for your connection
     * @param name   Name of user
     */
    public ClientHandler(Socket socket, String name, String type) {
        this.name = name;
        if (type.equals("CLI")) {
            this.type = userType.CLI;
        } else if (type.equals("GUI")) {
            this.type = userType.GUI;
        } else {
            throw new RuntimeException("User type does not exist");
        }

        try {
            this.socket = socket;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type.toString();
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
                System.out.println(name + ": " + message); // print to server console
                Server.broadcast(this, message);
            }
        } catch (IOException e) {
            System.out.println(name + " disconnected");
            Server.broadcast(this, "disconnected");
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
