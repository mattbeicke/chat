package mattb.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientGUI extends Application {
    private PrintWriter writer;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Chat");

        // setup chat history
        Label history = new Label();
        history.setWrapText(true);
        history.setPrefWidth(500);
        history.setMinWidth(500);
        history.setPadding(new Insets(0, 8, 0, 8));
        history.textProperty().addListener((_, _, _) -> {
            history.setMinHeight(Region.USE_PREF_SIZE);
        });
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(history);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(450);
        scrollPane.setPrefViewportWidth(500);
        history.heightProperty().addListener((_, _, _) -> {
            scrollPane.setVvalue(1.0d);
        });

        // input field
        TextField input = new TextField();
        input.setOnAction(_ -> {
            if (!input.getText().isBlank()) {
                writeMessage(input.getText());
                input.clear();
            }
        });

        Label welcome = new Label();
        VBox bottom = new VBox(2);
        bottom.getChildren().addAll(new Label("  Enter messages below (press enter to send):"), input);
        BorderPane border = new BorderPane(scrollPane, welcome, null, bottom, null);
        BorderPane.setAlignment(welcome, Pos.CENTER);
        BorderPane.setAlignment(history, Pos.BOTTOM_LEFT);
        Scene chat = new Scene(border, 500, 500);

        TextField name = new TextField();
        name.setOnAction(_ -> {
            if (!name.getText().isBlank()) {
                backend(history, name.getText());
                welcome.setText("Welcome " + name.getText());
                stage.setScene(chat);
            }
        });
        Label nameLabel = new Label("Enter your name (what you want to be displayed as) below:");
        VBox root = new VBox(nameLabel, name);
        root.setAlignment(Pos.CENTER);

        Scene nameScene = new Scene(root, 400, 300);

        stage.setScene(nameScene);
        stage.show();
        stage.setOnCloseRequest(_ -> {
            Platform.exit();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Initializes Client GUI's backend
     *
     * @param chat {@link Label} that contains the chat history
     * @param name user's inputted name (to be sent to server)
     */
    public void backend(Label chat, String name) {
        try {
            Socket socket = new Socket("localhost", 43206);
            MessageListener listener = new MessageListener(socket, chat);
            new Thread(listener).start();

            writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(name);
            writer.println("GUI");
        } catch (IOException | RuntimeException e) {
            System.out.println("Server went offline or other issue encountered");
        }
    }

    /**
     * Sends your chat to the server
     *
     * @param message what to send to the server
     */
    public void writeMessage(String message) {
        writer.println(message);
    }
}
