package mattb.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {
    enum userType {
        CLI, GUI
    }

    private Socket socket;
    private DataInputStream in;
    public DataOutputStream out;
    private final String name;
    private final userType type;

    /**
     * Initializes {@link ClientHandler ClientHandler's} reader and writer
     *
     * @param socket {@link Socket} used for your connection
     * @param name   Name of user
     */
    public ClientHandler(Socket socket, String name, String type, DataInputStream in) {
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
            this.in = in;
            this.out = new DataOutputStream(socket.getOutputStream());
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
        try {
            while (true) {
                int dataType = in.readInt();

                switch (dataType) {
                    case 1:
                        String message = in.readUTF();
                        System.out.println(name + ": " + message); // print to server console
                        Server.broadcast(this, name + ": " + message);
                        break;
                    case 2:
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("Left the server: " + name);
            Server.broadcast(this, "Left the server: " + name);
        } finally {
            closeEverything();
        }
    }

    /**
     * Shuts down the {@link ClientHandler ClientHandler's} {@link DataInputStream}, {@link DataOutputStream}, and {@link Socket}
     */
    private void closeEverything() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
