package com.DBMS.Frontend;

import com.DBMS.Backend.DataGetter.ConnectionGetter;
import com.DBMS.Backend.ObjectClass.LoginParameter;
import javafx.application.Application;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LoginPage extends Application {
    private TextField hostnameField;
    private TextField databaseField;
    private TextField usernameField;
    private PasswordField passwordField;
    private Label connectionHint;
    private Stage stage;


    public LoginPage() {
        this.hostnameField = new TextField();
        this.hostnameField.setPromptText("Sever Name:Port number)");
        this.databaseField = new TextField();
        this.usernameField = new TextField();
        this.passwordField = new PasswordField();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage s) throws Exception {
        this.stage = s;
        setupStage();
//        skipLogin(); // only used for debugging/developing MetricsPage

    }

    private void setupStage() {
        GridPane gr = new GridPane();
        gr.setStyle("-fx-background-color:#FFF5EE");
        addComponents(gr);

        gr.setAlignment(Pos.CENTER);
        gr.setHgap(10);
        gr.setVgap(15);

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(gr);
        this.connectionHint = new Label();
        borderPane.setBottom(connectionHint);
        BorderPane.setAlignment(connectionHint, Pos.BOTTOM_CENTER);
        BorderPane.setMargin(connectionHint, new Insets(0, 0, 60, 0));
        double sceneWidth = 400;
        double sceneHeight = 400;

        Scene scene = new Scene(borderPane);
        stage.setScene(scene);

        stage.setTitle("DBMS-Metrics");
        stage.setWidth(sceneWidth);
        stage.setHeight(sceneHeight);
        stage.setResizable(false); // set not to change window
        stage.initStyle(StageStyle.DECORATED);
        stage.show();

    }

    private void addComponents(GridPane gr) {

        Button connectButton = createConnectButton();
        Button clearButton = createClearButton();

        // add the components into the grid pane
        gr.add(new Label("Hostname:"), 0, 0);
        gr.add(new Label("Database:"), 0, 1);
        gr.add(new Label("Username:"), 0, 2);
        gr.add(new Label("Password:"), 0, 3);
        gr.add(hostnameField, 1, 0);
        gr.add(databaseField, 1, 1);
        gr.add(usernameField, 1, 2);
        gr.add(passwordField, 1, 3);
        gr.add(connectButton, 0, 4);
        gr.add(clearButton, 1, 4);

        // adjust the positions of the buttons
        GridPane.setMargin(connectButton, new Insets(30, 0, 0, 40));
        GridPane.setMargin(clearButton, new Insets(30, 0, 0, 50));
    }

    private Button createConnectButton() {
        Button connect = new Button("Connect");
        connect.setCursor(Cursor.CLOSED_HAND);
        ConnectionService connectionService = createService();

        connect.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent actionEvent) {

                connectionHint.setText("Connecting"); // it seems not to run after you unsuccessfully connect once

                if (connectionService.getState() == Worker.State.READY) {
                    connectionService.start();
                } else {
                    // we click the button more than once, use restart instead
                    connectionService.restart();
                }

            }
        });
        return connect;
    }

    private ConnectionService createService() {
        ConnectionService connectionService = new ConnectionService();
        connectionService.setOnSucceeded((event -> {
            if (connectionService.getValue().startConnection()) {
                // if connected, move to the next windows
                new MetricsPage(connectionService.getValue().connectionGetter());
                stage.close();
            } else {
                connectionHint.setText("Connection failed. " +
                        "Please check your entered information again. \n " +
                        "Make you have turned on the server and allow \n" +
                        "IP/TCP connection.");
            }
        }
        ));

        return connectionService;
    }


    class ConnectionService extends Service<ConnectionGetter> {

        @Override
        protected Task<ConnectionGetter> createTask() {
            Task<ConnectionGetter> task = new Task<ConnectionGetter>() {
                @Override
                protected ConnectionGetter call() throws Exception {
                    LoginParameter loginParameter = new LoginParameter(hostnameField.getText(), databaseField.getText(),
                            usernameField.getText(), passwordField.getText());
                    return new ConnectionGetter(loginParameter);
                }
            };
            return task;
        }
    }

    private Button createClearButton() {
        Button clear = new Button("Clear");
        clear.setCursor(Cursor.CLOSED_HAND);
        clear.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                hostnameField.setText("");
                databaseField.setText("");
                usernameField.setText("");
                passwordField.setText("");
            }
        });
        return clear;
    }

    private void skipLogin() {

        String hostName = "localhost:52353";
        String databaseName = "ReadingDBLog";
        String user = "sa";
        String passwords = "1Y`ckO\",";
        LoginParameter loginParameter = new LoginParameter(hostName, databaseName,
                user, passwords);
        ConnectionGetter connectionGetter = new ConnectionGetter(loginParameter);
        if (connectionGetter.startConnection()) {
            new MetricsPage(connectionGetter.connectionGetter());
            stage.close();
        }
    }
}
