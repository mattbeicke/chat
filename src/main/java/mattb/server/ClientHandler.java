package mattb.server;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader reader;
    public PrintWriter writer;
    private String name;

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
