package main.java.com.DBMS.Frontend;

import com.DBMS.Backend.ColumnsGetter;
import com.DBMS.Backend.Consistency;
import com.DBMS.Backend.TablesGetter;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import main.java.com.DBMS.Backend.Accuracy;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class MetricsPage {
    private Connection conn;

    public MetricsPage(Connection connection) {
        this.conn = connection;
        // todo: remember to close the connection after the search, set in exit function

        /*get the table only once*/
        TablesGetter tablesGetter = new TablesGetter(connection);
        ObservableList<String> tableList = FXCollections.observableList(tablesGetter.getTableList());

        /*initialize some choiceboxes that are not easily implemented in the parent class and its subclasses*/
        ChoiceBox choiceTable = new ChoiceBox(tableList);
        ChoiceBox choiceColumnAcc = new ChoiceBox();
        ChoiceBox choiceColumnCon1 = new ChoiceBox();
        ChoiceBox choiceColumnCon2 = new ChoiceBox();
        ChoiceBox choiceColLevel = new ChoiceBox();

        /*Change the columns list if change*/
        choiceTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue observable, String oldValue, String newValue) {

                /*via the connection to ask the relevant columns*/
                ColumnsGetter columnsGetter = new ColumnsGetter(connection, newValue);
                ObservableList<String> colList = FXCollections.observableList(columnsGetter.getColumns());

                /*update the value in all column choice box*/

                /*why do stupid copy-paste coding? Why not in grid class?
                The author is afraid the object will not get triggered when they
                * are initialized in each grid class*/

                choiceColumnAcc.setItems(colList);
                choiceColumnCon1.setItems(colList);
                choiceColumnCon2.setItems(colList);
                choiceColLevel.setItems(colList);
            }
        });


        /*put 4 metrics grids into the a big grid*/
        GridPane gridPane = new GridPane();
        gridPane.setHgap(50);
        gridPane.setVgap(50);

        // initialize the stage after Instantiation
        Stage stage = new Stage();
        AccuracyGrid accuracyGrid = new AccuracyGrid(stage, choiceColumnAcc);
        ConsistencyGrid consistencyGrid = new ConsistencyGrid(stage, choiceColumnCon1, choiceColumnCon2);
        CurrencyGrid currencyGrid = new CurrencyGrid();
        CompletenessGrid completenessGrid = new CompletenessGrid();

        gridPane.add(accuracyGrid.getGr(), 0, 0);
        gridPane.add(consistencyGrid.getGr(), 1, 0);
        gridPane.add(currencyGrid.getGr(), 0, 1);
        gridPane.add(completenessGrid.getGr(), 1, 1);


        /*combine the table grid with the big grid above in the borderPane with position adjustments*/
        BorderPane borderPane = new BorderPane();
        Subtitle subtitle = new Subtitle(choiceTable);
        GridPane tableGrid = subtitle.getGr();
        borderPane.setTop(tableGrid);
        borderPane.setMargin(tableGrid, new Insets(12, 12, 50, 12));
        borderPane.setCenter(gridPane);
        borderPane.setMargin(gridPane, new Insets(0, 0, 0, 30));

        /*create button to connect the backend*/
        Button submitButton = new Button("Submit");
        submitButton.setCursor(Cursor.CLOSED_HAND);
        submitButton.setPrefSize(80, 30);
        submitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (accuracyGrid.getCbTitle().isSelected()) {
                    callAccuracy(subtitle, accuracyGrid);
                }
                if (consistencyGrid.getCbTitle().isSelected()) {
                    callConsistency(subtitle, consistencyGrid);
                }
            }
        });

        borderPane.setBottom(submitButton);
        BorderPane.setMargin(submitButton, new Insets(0, 0, 30, 300));

        /*finally put all stuffs into the scene*/
        Scene scene = new Scene(borderPane);
        stage.setScene(scene);

        stage.setTitle("Metrics Selection");
//        stage.getIcons().add(new Image("main\\java\\com\\DBMS\\Frontend\\icon.png"));
        double sceneWidth = 850;
        double sceneHeight = 650;
        stage.setWidth(sceneWidth);
        stage.setHeight(sceneHeight);

        stage.setResizable(false); // set not to change window
        stage.initStyle(StageStyle.DECORATED);
        stage.show();
    }

    /*the following 4 functions connect to the backend code*/

    private void callAccuracy(Subtitle subtitle, AccuracyGrid accuracyGrid) {
        try {
            Statement statement = conn.createStatement();
            String tableName = (String) subtitle.tableCb.getValue();
//                        System.out.println(tableName);
            String accuracyCol = (String) accuracyGrid.examinedCol.getValue();
//                        System.out.println(accuracyCol);

            String pathText = accuracyGrid.getPathText().getText();

            String refNum = accuracyGrid.getRefText1().getText().trim(); // trim to ensure safe input

            Accuracy accuracy = new Accuracy(tableName, accuracyCol, pathText, refNum
                    , statement);
            System.out.println("Accuracy is: " + accuracy.calculation() + "\n");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void callConsistency(Subtitle subtitle, ConsistencyGrid consistencyGrid) {
        try {
            Statement statement = conn.createStatement();
            String tableName = (String) subtitle.tableCb.getValue();
//                        System.out.println(tableName);
            String consistencyCol1 = (String) consistencyGrid.choiceColumn1.getValue();
//                        System.out.println(accuracyCol);

            String consistencyCol2 = (String) consistencyGrid.choiceColumn2.getValue();

            String pathText = consistencyGrid.getPathText().getText();

            String refNum1 = consistencyGrid.getRefText1().getText().trim(); // trim to ensure safe input
            String refNum2 = consistencyGrid.getRefText2().getText().trim(); // trim to ensure safe input

            Consistency consistency = new Consistency(tableName, consistencyCol1, consistencyCol2, pathText,
                    refNum1, refNum2, statement); // maybe it is
            System.out.println("Consistency is: " + consistency.calculation() + "\n");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*The following the 5 subclasses inherit the similar grid parent class, four of them are the metrics grid*/

    /*the first grid "Choose the Table"*/
    class Subtitle extends main.java.com.DBMS.Frontend.GridFormat {
        ChoiceBox tableCb;

        Subtitle(ChoiceBox choiceTable) {
            tableCb = choiceTable;
            setHintTitle("First of all, choose the examined table:\n" +
                    "If possible, name columns without dots");
            componentAdder(getHintTitle(), 0, 1);
            choiceTable.setUserData("table"); // make sure to be enabled at the first place
            componentAdder(tableCb, 1, 1);

        }
    }

    /*choose accuracy*/
    class AccuracyGrid extends main.java.com.DBMS.Frontend.GridFormat {
        ChoiceBox examinedCol;

        AccuracyGrid(Stage stage, ChoiceBox choiceColumn) {

            /*initialize*/
            this.examinedCol = choiceColumn;
            setFileButton(stage);
            setHintTitle("Accuracy");
            setCbTitle();
            setPathText();
            setRefText1();

            /*Add to the grid*/
            componentAdder(getCbTitle(), 0, 0);
            componentAdder(getHintTitle(), 1, 0);
            componentAdder(new Label("Reference"), 0, 1);
            componentAdder(getPathText(), 1, 1);
            componentAdder(getFileButton(), 2, 1);
            componentAdder(new Label("examined column"), 0, 2);
            componentAdder(this.examinedCol, 1, 2);
            componentAdder(new Label("Reference's column \n (start from 1): "), 0, 3);
            componentAdder(getRefText1(), 1, 3);
        }

    }

    /*choose consistency*/
    class ConsistencyGrid extends main.java.com.DBMS.Frontend.GridFormat {
        ChoiceBox choiceColumn1;
        ChoiceBox choiceColumn2;

        ConsistencyGrid(Stage stage, ChoiceBox choiceColumn1, ChoiceBox choiceColumn2) {

            /*Initialize*/
            setFileButton(stage);
            setCbTitle();
            setHintTitle("Consistency");
            setPathText();
            this.choiceColumn1 = choiceColumn1;
            this.choiceColumn2 = choiceColumn2;
            setRefText1();
            setRefText2();

            /*Add to the grid*/
            componentAdder(getCbTitle(), 0, 0);
            componentAdder(getHintTitle(), 1, 0);
            componentAdder(new Label("Reference"), 0, 1);
            componentAdder(getPathText(), 1, 1);
            componentAdder(getFileButton(), 2, 1);
            componentAdder(new Label("examined column's\n" +
                    "Antecedent and Consequent"), 0, 2);
            componentAdder(choiceColumn1, 1, 2);
            componentAdder(choiceColumn2, 2, 2);
            componentAdder(new Label("Reference's Antecedent\n" +
                    "and Consequent (number): "), 0, 3);
            componentAdder(getRefText1(), 1, 3);
            componentAdder(getRefText2(), 2, 3);
            componentAdder(new Label("Follow the order of \n" +
                    "Antecedent->Consequent! "), 0, 4);
        }
    }

    /*choose Currency*/
    class CurrencyGrid extends main.java.com.DBMS.Frontend.GridFormat {
        CurrencyGrid() {
            /*Initialize*/
            setCbTitle();
            setHintTitle("Currency");
            setCbRow();
            setRefText1();
            setCbTable();

            /*Add to the grid*/
            componentAdder(getCbTitle(), 0, 0);
            componentAdder(getHintTitle(), 1, 0);
            componentAdder(getCbRow(), 0, 1);
            componentAdder(new Label("Row Level"), 1, 1);
            componentAdder(new Label("Primary Key\n (number):  "), 2, 1);
            componentAdder(getRefText1(), 3, 1);
            componentAdder(getCbTable(), 0, 2);
            componentAdder(new Label("Table level"), 1, 2);
        }
    }

    /*choose Completeness*/
    class CompletenessGrid extends main.java.com.DBMS.Frontend.GridFormat {
        CompletenessGrid() {

            /*Initialize*/
            setCbTitle();
            setHintTitle("Completeness");
            setCbRow();
            setRefText1();
            setCbAttribute();
            setCbTable();

            /*Add to the grid*/
            componentAdder(getCbTitle(), 0, 0);
            componentAdder(getHintTitle(), 1, 0);
            componentAdder(getCbRow(), 0, 1);
            componentAdder(new Label("Row Level"), 1, 1);
            componentAdder(new Label("Primary Key\n (number):  "), 2, 1);
            componentAdder(getRefText1(), 3, 1);
            componentAdder(getCbAttribute(), 0, 2);
            componentAdder(new Label("Attribute Level"), 1, 2);
            componentAdder(getCbTable(), 0, 3);
            componentAdder(new Label("Table level"), 1, 3);
        }
    }
}
