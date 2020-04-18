package com.DBMS.Frontend;

import com.DBMS.Backend.MssqlConnection;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class LoginPage extends Application {

    public static void main(String[] args) {
        launch(args);
    }


    public void componentAdder(GridPane gr, Stage s) {

        Label hostname = new Label("Hostname:");
        Label database = new Label("Database:");
        Label username = new Label("Username:");
        Label password = new Label("Password:");

        TextField t_hostname = new TextField(); //todo: default host is....
        TextField t_database = new TextField();
        TextField t_username = new TextField();
        PasswordField t_password = new PasswordField();

        Button login = new Button("Connect");
        login.setCursor(Cursor.CLOSED_HAND);
        login.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

//                System.out.println(t_hostname.getText()); // for debugging
//                System.out.println(t_database.getText());
//                System.out.println(t_username.getText());
//                System.out.println(t_password.getText());

                // todo:if empty, hint the user to fill all the information

                MssqlConnection mssqlConnection = new MssqlConnection(t_hostname.getText(), t_database.getText(),
                        t_username.getText(), t_password.getText());


                if (mssqlConnection.startConnection()) {
                    // if connected, move to the next windows
                    MetricsPage metricsPage = new MetricsPage(mssqlConnection.connectionGetter());

                    s.close();
                } else {
                    System.out.println("Connection failed. " +
                            "Please check your entered information again. Make sure\n" +
                            " you have turned on the server and allow IP/TCP connection.");
                    // todo: put it as Help text under the button
                }

                //todo close it if successful?
            }
        });

        Button clear = new Button("Clear");
        clear.setCursor(Cursor.CLOSED_HAND);
        clear.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                textFieldclear(t_hostname);
                textFieldclear(t_database);
                textFieldclear(t_username);
                textFieldclear(t_password);
            }
        });

        // add the components into the grid pane
        gr.add(hostname, 0, 0);
        gr.add(database, 0, 1);
        gr.add(username, 0, 2);
        gr.add(password, 0, 3);
        gr.add(t_hostname, 1, 0);
        gr.add(t_database, 1, 1);
        gr.add(t_username, 1, 2);
        gr.add(t_password, 1, 3);
        gr.add(login, 0, 4);
        gr.add(clear, 1, 4);

        // adjust the positions of the buttons
        GridPane.setMargin(login, new Insets(30, 0, 0, 40));
        GridPane.setMargin(clear, new Insets(30, 0, 0, 50));
    }

    private void textFieldclear(TextField textField) {
        textField.setText("");
    }

    @Override
    public void start(Stage s) throws Exception {
        /********************************************************************/
        /* the following code is only used for debugging/developing MetricsPage*/

        String serverName = "localhost:52353";
        String databaseName = "ReadingDBLog";
        String user = "sa";
        String passwords = "1Y`ckO\",";
        MssqlConnection mssqlConnection = new MssqlConnection(serverName, databaseName,
                user, passwords);
        if (mssqlConnection.startConnection()) {
            new MetricsPage(mssqlConnection.connectionGetter());
            s.close();
        }


        /*********************************************************************/
        /*if you debug using the code above, remember to comment the following code in order to
        disable the main page and directly go to the metrics page!
         */
//
//        GridPane gr = new GridPane();
//        gr.setStyle("-fx-background-color:#FFF5EE");
//        componentAdder(gr, s);
//
//        gr.setAlignment(Pos.CENTER);
//        gr.setHgap(10);
//        gr.setVgap(15);
//
//
//        double sceneWidth = 500;
//        double sceneHeight = 500;
//
//
//        Scene scene = new Scene(gr);
//        s.setScene(scene);
//
//        s.setTitle("DBMS-Metrics");
//        s.setWidth(sceneWidth);
//        s.setHeight(sceneHeight);
//        s.setResizable(false); // set not to change window
//        s.initStyle(StageStyle.DECORATED);
//        s.show();
//

        /*********************************************************************/
    }
}
