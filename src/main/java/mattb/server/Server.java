package mattb.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Server that handles clients initial connection before passing them to a {@link Thread}
 *
 * @author Matthew Beicke
 */
public class Server {
    public static List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    static void main() {
        try (ServerSocket serverSocket = new ServerSocket(43206)) {
            System.out.println("Server Online (Type '-shutdown' to stop)");

            Thread consoleThread = new Thread(() -> {
                try (Scanner scanner = new Scanner(System.in)) {
                    while (true) {
                        String input = scanner.nextLine();
                        if ("-shutdown".equals(input)) { // Initiates shut down procedure (see below)
                            shutdown(serverSocket);
                            break;
                        }
                        // Add other server-level commands here
                    }
                }
            });
            consoleThread.setDaemon(true);
            consoleThread.start();

            while (!serverSocket.isClosed()) {
                try {
                    Socket socket = serverSocket.accept();
                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    String name = in.readUTF(); // First thing sent will always be name
                    String type = in.readUTF(); // Second thing sent will always be type

                    ClientHandler handler = new ClientHandler(socket, name, type, in);
                    clients.add(handler);
                    new Thread(handler).start(); // Initialize ClientHandler in its own Thread

                    System.out.println("Joined the server: " + name);
                    broadcastMessage(handler, "Joined the server: " + name);
                } catch (IOException e) {
                    if (serverSocket.isClosed()) {
                        System.out.println("Server closed successfully.");
                    } else {
                        System.out.println("Server went offline or other issue encountered");
                    }
                }
            }
        } catch (IOException ignored) {
            System.out.println("Server went offline or other issue encountered");
        }
    }

    /**
     * Broadcasts sent messages to all clients
     * (unless client is using CLI then it does not send back to itself)
     *
     * @param clientHandler Client that sent the message
     * @param message       Message they sent
     */
    public static void broadcastMessage(ClientHandler clientHandler, String message) {
        for (ClientHandler client : clients) { // Send to all clients
            if (client.getType().equals(ClientHandler.clientType.CLI.toString())) {
                if (clientHandler == client) continue;
            }

            try {
                client.out.writeInt(1);
                client.out.writeUTF(message);
                client.out.flush();
            } catch (IOException e) {
                removeHandler(client); // Client must have disconnected, just updating the clients List here
            }
        }
    }

    /**
     * Sends list of all current connected users to the requesting client
     *
     * @param clientHandler Client who requested user list
     */
    public static void sendUsers(ClientHandler clientHandler) {
        StringBuilder out = new StringBuilder();

        for (ClientHandler client : clients) {
            if (clientHandler == client) {
                out.append(client.getName()).append(" (you)\n"); // Indicates this user is you
            } else {
                out.append(client.getName()).append("\n");
            }
        }

        try {
            clientHandler.out.writeInt(2);
            clientHandler.out.writeUTF(String.valueOf(out));
            clientHandler.out.flush();
        } catch (IOException ignored) {
        }
    }

    /**
     * Removes specified handler from the client list (after it disconnects)
     *
     * @param clientHandler Client to remove
     */
    public static void removeHandler(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    /**
     * Shuts down the server gracefully, also shutting down all connected clients too
     *
     * @param serverSocket {@link ServerSocket} that the server is running
     */
    public static void shutdown(ServerSocket serverSocket) {
        System.out.println("Shutting down all client connections...");

        for (ClientHandler client : clients) { // Sends shut down command and message to all clients
            try {
                client.out.writeInt(0);
                client.out.writeUTF("Server is shutting down.");
                client.out.flush();
            } catch (IOException ignored) {
            }
        }

        clients.clear();

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
    }
}