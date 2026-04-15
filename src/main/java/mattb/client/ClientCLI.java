package mattb.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ClientCLI {
    static void main() {
        Scanner input = new Scanner(System.in);
        String name = "";
        do {
            System.out.println("What name do you want to use?");
            name = input.nextLine();
        } while (name.isBlank());

        try {
            Socket socket = new Socket("localhost", 43206);
            MessageListener listener = new MessageListener(socket);
            new Thread(listener).start();

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(name);
            out.writeUTF("CLI");
            out.flush();

            System.out.println("Enter '-exit' to leave or -help to view list of commands");
            boolean set = false;
            String prev = "";
            while (true) {
                String message = input.nextLine();

                if (set && prev.equals(message)) {
                    out.writeInt(1);
                    out.writeUTF(message);
                    out.flush();
                    set = false;
                } else if (message.equals("-exit")) {
                    listener.shutdown();
                    break;
                } else if (message.equals("-users")) {
                    try {
                        out.writeInt(2);
                        out.flush();
                    } catch (IOException e) {
                        System.out.println("Server went offline or other issue encountered");
                    }
                } else if (message.equals("-help")) {
                    System.out.println("""
                            Commands Available:
                            -exit  : quits Chat
                            -users : displays all online users
                            -help  : displays this list
                            """);
                } else if (message.charAt(0) == '-') {
                    System.out.println("Unknown Command '" + message + "'. Enter it again to send it or enter -help to see all available commands");
                    prev = message;
                    set = true;
                } else {
                    out.writeInt(1);
                    out.writeUTF(message);
                    out.flush();
                    set = false;
                }
            }
        } catch (IOException | RuntimeException e) {
            System.out.println("Server went offline or other issue encountered");
        }
    }
}