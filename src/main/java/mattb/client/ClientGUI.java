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

public class ClientGUI extends Application {
    private ClientGUIBackend cGUIB;

    @Override
    public void start(Stage stage) {
        cGUIB = null;
        stage.setTitle("Chat");

        /**
         * setup chat history
         */
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
                cGUIB.writeMessage(input.getText());
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
            Platform.exit();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
