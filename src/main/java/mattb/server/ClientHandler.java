package mattb.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * This class is used to handle receiving and redistributing input from the Clients
 *
 * @author Matthew Beicke
 */
public class ClientHandler implements Runnable {
    /**
     * Clients are either using a GUI or CLI
     */
    enum clientType {
        CLI, GUI
    }

    private Socket socket; // The server side of a client's socket
    private DataInputStream in; // Where messages from the client come from
    public DataOutputStream out; // Where message to the client go
    private final String name; // Client's name
    private final clientType type; // Client's type (from the enum above)

    /**
     * Initializes the {@link DataInputStream} and {@link DataOutputStream} for {@link ClientHandler this}
     *
     * @param socket {@link Socket} used for your connection
     * @param name   Name of client
     * @param type   Client type (see {@link clientType})
     * @param in     The {@link DataInputStream} that is to be used to receive data from the client
     */
    public ClientHandler(Socket socket, String name, String type, DataInputStream in) {
        this.name = name;
        if (type.equals("CLI")) { // Determine which in the enum to used
            this.type = clientType.CLI;
        } else if (type.equals("GUI")) {
            this.type = clientType.GUI;
        } else {
            throw new RuntimeException("Client type does not exist");
        }

        try {
            this.socket = socket;
            this.in = in;
            this.out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Server went offline or other issue encountered");
        }
    }

    /**
     * Getter for client name
     *
     * @return Name of client
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for client type (CLI or GUI)
     *
     * @return Type of client
     */
    public String getType() {
        return type.toString();
    }

    /**
     * Prints out whatever is recieved from the client to the {@link Server} console and also initiates the broadcast of it to all other clients
     * It also prints and broadcasts a message when a client disconnects
     */
    @Override
    public void run() {
        try {
            while (true) { // While it may be bad programming technically, changing this to not be a while (true) will not fix anything as nothing is broken
                int dataType = in.readInt();

                switch (dataType) {
                    case 1: // User sends a message
                        String message = in.readUTF();
                        System.out.println(name + ": " + message); // Print to server console
                        Server.broadcastMessage(this, name + ": " + message);
                        break;
                    case 2: // User wants user list
                        Server.sendUsers(this);
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("Left the server: " + name);
            Server.broadcastMessage(this, "Left the server: " + name);
            Server.removeHandler(this);
        } finally {
            closeEverything();
        }
    }

    /**
     * Closes the {@link DataInputStream}, {@link DataOutputStream}, and {@link Socket} for {@link ClientHandler this}
     */
    private void closeEverything() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.out.println("Server went offline or other issue encountered");
        }
    }
}
