package mattb.client;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ClientGUI extends Application {
    private ClientGUIBackend cGUIB;

    @Override
    public void start(Stage stage) {
        cGUIB = null;
        stage.setTitle("Chat");

        Label history = new Label();
        history.setWrapText(true);
        TextField input = new TextField();
        input.setOnAction(_ -> {
            if (!input.getText().isBlank()) {
                cGUIB.writeMessage(input.getText());
                input.clear();
            }
        });
        Label welcome = new Label();
        VBox bottom = new VBox(2);
        bottom.getChildren().addAll(new Label("  Enter messages below (press enter to send):"), input);
        BorderPane border = new BorderPane(history, welcome, null, bottom, null);
        BorderPane.setAlignment(welcome, Pos.CENTER);
        Scene chat = new Scene(border, 500, 500);

        TextField name = new TextField();
        name.setOnAction(_ -> {
            if (!name.getText().isBlank()) {
                cGUIB = new ClientGUIBackend(history, name.getText());
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
            if (cGUIB != null) {
                cGUIB.shutdown();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
