package mattb.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    public static List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    static void main() {
        try {
            ServerSocket serverSocket = new ServerSocket(43206);
            System.out.println("Server Online");
            while (true) {
                Socket socket = serverSocket.accept();
                DataInputStream in = new DataInputStream(socket.getInputStream());
                String name = in.readUTF();
                String type = in.readUTF();

                ClientHandler handler = new ClientHandler(socket, name, type, in);
                clients.add(handler);
                Thread thread = new Thread(handler);
                thread.start();

                System.out.println("Joined the server: " + name);
                broadcastMessage(handler, "Joined the server: " + name);
            }
        } catch (IOException | RuntimeException e) {
            System.out.println("Server went offline or other issue encountered");
        }
    }

    /**
     * Broadcasts clients sent messages to all other clients (except the sender)
     *
     * @param clientHandler Client that sent the message
     * @param message       Message they sent
     */
    public static void broadcastMessage(ClientHandler clientHandler, String message) {
        for (ClientHandler client : clients) {
            if (client.getType().equals(ClientHandler.userType.CLI.toString())) {
                if (clientHandler == client) continue;
            }

            try {
                client.out.writeInt(1);
                client.out.writeUTF(message);
                client.out.flush();
            } catch (IOException e) {
                clients.remove(client);
            }
        }
    }

    /**
     * sends list of all current connected users to who requested it
     *
     * @param clientHandler client who requested user list
     */
    public static void sendUsers(ClientHandler clientHandler) {
        StringBuilder out = new StringBuilder();

        for (ClientHandler client : clients) {
            if (clientHandler == client) {
                out.append(client.getName()).append(" (you)\n");
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
     * @param clientHandler client to remove
     */
    public static void removeHandler(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }
}