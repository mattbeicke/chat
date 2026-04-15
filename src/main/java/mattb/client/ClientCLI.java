package mattb.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ClientCLI {
    static void main() {
        Scanner input = new Scanner(System.in);
        System.out.println("What name do you want to use?");
        String name = input.nextLine();

        try {
            Socket socket = new Socket("localhost", 43206);
            MessageListener listener = new MessageListener(socket);
            new Thread(listener).start();

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(name);
            out.writeUTF("CLI");
            out.flush();

            System.out.println("Enter '-exit' to leave");
            while (true) {
                String message = input.nextLine();
                if (message.equals("-exit")) {
                    listener.shutdown();
                    break;
                }
                out.writeInt(1);
                out.writeUTF(message);
                out.flush();
            }
        } catch (IOException | RuntimeException e) {
            System.out.println("Server went offline or other issue encountered");
        }
    }
}