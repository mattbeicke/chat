package mattb.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import mattb.ChatError;
import mattb.ChatException;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Graphical User Interface for displaying the chat
 *
 * @author Matthew Beicke
 */
public class ClientGUI extends Application {
    private DataOutputStream out; // Output stream, used when sending your chats to the server

    /**
     * Initialization for the GUI
     *
     * @param stage the primary stage for this application, onto which
     *              the application scene can be set.
     *              Applications may create other stages, if needed, but they will not be
     *              primary stages.
     */
    @Override
    public void start(Stage stage) {
        stage.setTitle("Chat");

        // Setup chat history Label
        Label history = new Label();
        history.setWrapText(true);
        history.setPrefWidth(500);
        history.setMinWidth(500);
        history.setPadding(new Insets(0, 8, 0, 8));
        history.textProperty().addListener((_, _, _) -> history.setMinHeight(Region.USE_PREF_SIZE));
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(history);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(450);
        scrollPane.setPrefViewportWidth(500);
        history.heightProperty().addListener((_, _, _) -> scrollPane.setVvalue(1.0d));

        // Input field
        TextField input = new TextField();
        input.setOnAction(_ -> {
            if (!input.getText().isBlank()) {
                writeMessage(input.getText());
                input.clear();
            }
        });

        // Set up top bar
        Label welcome = new Label();
        Button whoOnline = new Button("Who's Online");
        whoOnline.setOnAction(_ -> {
            try {
                out.writeInt(2);
                out.flush();
            } catch (IOException e) {
                throw new ChatException(ChatError.CLIENT_SEND_FAILED);
            }
        });
        HBox hBox = new HBox(welcome, whoOnline);
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(10);

        // Set up bottom bar
        VBox bottom = new VBox(2);
        bottom.getChildren().addAll(new Label("  Enter messages below (press enter to send):"), input);

        // Set up BorderPane
        BorderPane border = new BorderPane(scrollPane, hBox, null, bottom, null);
        BorderPane.setAlignment(hBox, Pos.CENTER);
        BorderPane.setAlignment(history, Pos.BOTTOM_LEFT);
        Scene chat = new Scene(border, 500, 500);

        // Process user entering name
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
     * Initializes Client GUI's backend (setting up {@link Socket} and {@link MessageListener})
     *
     * @param chat {@link Label} that contains the chat history
     * @param name User's inputted name (to be sent to server)
     */
    private void backend(Label chat, String name) {
        Socket socket;
        try {
             socket = new Socket("localhost", 43206);
            MessageListener listener = new MessageListener(socket, chat);
            new Thread(listener).start();
        } catch (IOException e) {
            throw new ChatException(ChatError.CLIENT_CONNECTION_FAILED);
        }

        try{
            out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(name);
            out.writeUTF("GUI");
            out.flush();
        } catch (IOException e) {
            throw new ChatException(ChatError.CLIENT_SEND_FAILED);
        }
    }

    /**
     * Sends your chat to the server
     *
     * @param message The message you sent in the chat
     */
    private void writeMessage(String message) {
        try {
            out.writeInt(1);
            out.writeUTF(message);
            out.flush();
        } catch (IOException e) {
            throw new ChatException(ChatError.CLIENT_SEND_FAILED);
        }
    }
}
