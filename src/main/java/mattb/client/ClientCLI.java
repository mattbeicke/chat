package mattb.client;

import mattb.ChatError;
import mattb.ChatException;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

/**
 * Command Line Interface for displaying the chat
 *
 * @author Matthew Beicke
 */
public class ClientCLI {
    static void main() {
        main(43206);
    }

    /**
     * Split from regular main above because of tests
     *
     * @param port Port to use for server socket
     */
    static void main(int port) {
        Scanner input = new Scanner(System.in);
        String name;
        DataOutputStream out;
        MessageListener listener;
        do {
            System.out.println("What name do you want to use?");
            name = input.nextLine();
        } while (name.isBlank()); // Force user to enter a name that can be displayed to other users

        try {
            Socket socket = new Socket("localhost", port);
            listener = new MessageListener(socket);
            new Thread(listener).start();

            out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(name);
            out.writeUTF("CLI");
            out.flush();
        } catch (IOException e) {
            throw new ChatException(ChatError.CLIENT_CONNECTION_FAILED);
        }

        try {
            System.out.println("Enter '-exit' to leave or -help to view list of commands");
            boolean set = false; // Used when deciding to send a message that starts with a '-' or not
            String prev = "";
            while (true) {
                String message = input.nextLine();

                if (set && prev.equals(message)) { // User wants to send a message that starts with a '-' so send it
                    out.writeInt(1);
                    out.writeUTF(message);
                    out.flush();
                    set = false;
                } else if (message.equals("-exit")) { // User initiates shut down
                    listener.shutdown();
                    break;
                } else if (message.equals("-users")) { // User requests user list
                    out.writeInt(2);
                    out.flush();
                } else if (message.equals("-help")) { // User wants to see all available commands
                    System.out.println();
                    System.out.println("""
                            Commands Available:
                            -exit  : quits Chat
                            -users : displays all online users
                            -help  : displays this list
                            """);
                } else if (message.charAt(0) == '-') { // Commands should start with a '-'
                    System.out.println("Unknown Command '" + message + "'. Enter it again to send it or enter -help to see all available commands");
                    prev = message;
                    set = true;
                } else { // User just wanted to send a message
                    out.writeInt(1);
                    out.writeUTF(message);
                    out.flush();
                    set = false;
                }
            }
        } catch (IOException e) {
            throw new ChatException(ChatError.CLIENT_SEND_FAILED);
        }
    }
}