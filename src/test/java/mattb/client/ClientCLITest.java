package mattb.client;

import mattb.ChatError;
import mattb.ChatException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ClientCLITest {
    private static InputStream originalIn = null;
    private static PrintStream originalOut = null;

    @BeforeAll
    static void save() {
        originalIn = System.in;
        originalOut = System.out;
    }

    @AfterEach
    void restore() {
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    @Test
    void testConnectionFailure() {
        String simulatedInput = "TestUser\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try {
            ClientCLI.main();
            fail("Should have thrown ChatException");
        } catch (ChatException thrown) {
            assertEquals(ChatError.CLIENT_CONNECTION_FAILED, thrown.getError());
        }
    }

    @Test
    void testNameFail() {
        String simulatedInput = "\nTestUser\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try {
            ClientCLI.main();
        } catch (ChatException ignored) {
        }

        String capturedOutput = outContent.toString();
        long count = Arrays.stream(capturedOutput.split("\n"))
                .filter(line -> line.contains("What name do you want to use?"))
                .count();
        assertEquals(2L, count);
    }

    @Test
    void testFullCommandLoop() throws Exception {
        CountDownLatch serverReady = new CountDownLatch(1);

        try (ServerSocket server = new ServerSocket(0)) {
            int port = server.getLocalPort();
            serverReady.countDown();

            Thread serverThread = new Thread(() -> {
                try (Socket clientSocket = server.accept()) {
                    DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                    System.err.println(in.readUTF());
                    System.err.println(in.readUTF());
                    System.err.println(in.readInt());
                    System.err.println(in.readUTF());
                    System.err.println(in.readInt());
                    System.err.println(in.readUTF());
//                    assertEquals("TestUser", in.readUTF());
//                    assertEquals("CLI", in.readUTF());
//                    assertEquals(1, in.readInt());
//                    assertEquals("-notacommand", in.readUTF());
//                    assertEquals(2, in.readInt());
//                    assertEquals("a", in.readUTF());

                    Thread.sleep(1000);
                } catch (Exception ignored) {
                }
            });
            serverThread.setDaemon(true);
            serverThread.start();

            serverReady.await(2, TimeUnit.SECONDS);

            String input = "TestUser\n-help\n-notacommand\n-notacommand\n-users\n-exit\n";
            System.setIn(new ByteArrayInputStream(input.getBytes()));

            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outContent));

            ClientCLI.main(port);

            String output = outContent.toString();
            assert (output.contains("Commands Available:"));
            assert (output.contains("Unknown Command"));
        }
    }
}