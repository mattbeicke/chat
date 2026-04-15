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

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(43206);
            while (true) {
                Socket socket = serverSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String name = reader.readLine();
                System.out.println(name + " connected!");

                ClientHandler handler = new ClientHandler(socket, name);
                clients.add(handler);
                Thread thread = new Thread(handler);
                thread.start();
            }
        } catch (IOException e) {
            System.out.println("Error Has occurred");
        }
    }

    public static void broadcast(ClientHandler clientHandler, String message) {
        for (ClientHandler client : Server.clients) {
            if (clientHandler == client) continue;
            try {
                client.writer.println(message);
            } catch (Exception e) {
                Server.clients.remove(client);
            }
        }
    }

}