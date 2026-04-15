package mattb.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String name = reader.readLine();
                String type = reader.readLine();
                System.out.println(name + " connected!");

                ClientHandler handler = new ClientHandler(socket, name, type);
                clients.add(handler);
                Thread thread = new Thread(handler);
                thread.start();
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
                client.writer.println(clientHandler.getName() + ": " + message);
            } catch (Exception e) {
                Server.clients.remove(client);
            }
        }
    }
}