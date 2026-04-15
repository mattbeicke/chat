package mattb.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.println("What name do you want to use?");
        String name = input.nextLine();

        try {
            Socket socket = new Socket("localhost", 43206);
            MessageListener listener = new MessageListener(socket);
            new Thread(listener).start();

            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(name);

            while (true) {
                writer.println(input.nextLine());
            }
        } catch (IOException | RuntimeException e) {
            System.out.println("Server went offline or other issue encountered");
        }
    }
}