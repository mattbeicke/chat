package mattb.client;

import javafx.scene.control.Label;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientGUIBackend {
    private Socket socket;
    private PrintWriter writer;

    public ClientGUIBackend(Label chat, String name) {
        try {
            socket = new Socket("localhost", 43206);
            MessageListener listener = new MessageListener(socket, chat);
            new Thread(listener).start();

            writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(name);
            writer.println("GUI");
        } catch (IOException | RuntimeException e) {
            System.out.println("Server went offline or other issue encountered");
        }
    }

    public void writeMessage(String message) {
        writer.println(message);
    }

    public void shutdown() {
        try {
            if (writer != null) writer.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
