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
                broadcast(handler, "Joined the server: " + name);
            }
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            System.out.println("An error has occurred");
        }
    }

    /**
     * Broadcasts clients sent messages to all other clients (except the sender)
     *
     * @param clientHandler Client that sent the message
     * @param message       Message they sent
     */
    public static void broadcast(ClientHandler clientHandler, String message) {
        for (ClientHandler client : Server.clients) {
            if (client.getType().equals(ClientHandler.userType.CLI.toString())) {
                if (clientHandler == client) continue;
            }

            try {
                client.out.writeInt(1);
                client.out.writeUTF(message);
                client.out.flush();
            } catch (Exception e) {
                Server.clients.remove(client);
            }
        }
    }
}