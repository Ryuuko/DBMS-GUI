package com.DBMS.Frontend;

import com.DBMS.Backend.*;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class MetricsPage {
    private Connection conn;
    private SimpleListProperty<String> colListProp;
    private ChoiceBox choiceTable;
    private Stage stage;

    /*Create the Grid classes for the metrics*/
    private AccuracyGrid accuracyGrid;
    private ConsistencyGrid consistencyGrid;
    private CurrencyGrid currencyGrid;
    private CompletenessGrid completenessGrid;

    public MetricsPage(Connection connection) {

        /*initialize the member variables*/

        this.conn = connection;  // todo: remember to close the connection after the search, set in exit function
        this.stage = new Stage();
        ObservableList<String> colList = FXCollections.observableArrayList();
        this.colListProp = new SimpleListProperty<String>(colList);
        this.choiceTable = createChoiceBoxTable();

        /*put 4 metrics grids into the a big grid*/
        GridPane metricsGrids = createMetricsCollection();

        this.accuracyGrid = new AccuracyGrid();
        this.consistencyGrid = new ConsistencyGrid();
        this.currencyGrid = new CurrencyGrid();
        this.completenessGrid = new CompletenessGrid();

        metricsGrids.add(accuracyGrid.getGr(), 0, 0);
        metricsGrids.add(consistencyGrid.getGr(), 1, 0);
        metricsGrids.add(currencyGrid.getGr(), 0, 1);
        metricsGrids.add(completenessGrid.getGr(), 1, 1);

        /*combine the table grid with the big grid above in the borderPane with position adjustments*/

        GridPane tableGrid = createTableGird();

        /*create button to connect the backend*/
        Button submitButton = createButton();

        BorderPane borderPane = createBorderPane(tableGrid, metricsGrids, submitButton);

        /*finally put all stuffs into the scene*/
        createStage(borderPane);
    }

    private ChoiceBox createChoiceBoxTable() {
        ChoiceBox choiceTable = new ChoiceBox(buildTableList());
        bindColListWithTableName(choiceTable);
        return choiceTable;
    }

    private ObservableList<String> buildTableList() {
        TableListGetter tableListGetter = new TableListGetter(this.conn);
        ObservableList<String> tableList = FXCollections.observableList(tableListGetter.getTableList());
        return tableList;
    }

    private void bindColListWithTableName(ChoiceBox choiceBoxTable) {
        ObservableMap<String, ObservableList<String>> tableColumnMap = FXCollections.observableHashMap();
        choiceBoxTable.getSelectionModel().selectedItemProperty().addListener((ChangeListener<String>)
                (observable, oldValue, newValue) -> {
                    /*We build the name-list to reduce unnecessary visit to the database connection*/
                    if (!tableColumnMap.containsKey(newValue)) {
                        /*via the connection to ask the relevant columns*/
                        ColumnsGetter columnsGetter = new ColumnsGetter(conn, newValue);
                        ObservableList<String> newObsList =
                                FXCollections.observableArrayList(columnsGetter.getColumns());
                        tableColumnMap.put(newValue, newObsList);
                    }

                    colListProp.set(tableColumnMap.get(newValue)); // we change the property, with which the choice box is bound

//                System.out.println(colListProp.getValue()); // for debugging
//                System.out.println(choiceColLevel.itemsProperty().isBound());
                });
    }

    private GridPane createTableGird() {
        GridPane gr = new GridPane();
        Label hint = new Label();
        hint.setText("First of all, choose the examined table:\n" +
                "If possible, name columns without dots");
        gr.add(hint, 0, 1);
        gr.add(choiceTable, 1, 1);
        return gr;
    }

    private GridPane createMetricsCollection() {
        GridPane metricsGrids = new GridPane();
        metricsGrids.setHgap(50);
        metricsGrids.setVgap(50);
        return metricsGrids;
    }

    /*The following the 4 subclasses inherit the similar grid parent class, four of them are the metrics grid*/

    /*choose accuracy*/
    class AccuracyGrid extends GridFormat {
        ChoiceBox examinedCol;

        AccuracyGrid() {

            /*initialize*/
            this.examinedCol = createChoiceBoxCols();
            setFileButton(stage);
            setHintTitle("Accuracy");
            setCbTitle();
            setPathText();
            setRefText1();

            /*Add to the grid*/
            addComponent(getCbTitle(), 0, 0);
            addComponent(getHintTitle(), 1, 0);
            addComponent(new Label("Reference"), 0, 1);
            addComponent(getPathText(), 1, 1);
            addComponent(getFileButton(), 2, 1);
            addComponent(new Label("examined column"), 0, 2);
            addComponent(this.examinedCol, 1, 2);
            addComponent(new Label("Reference's column \n (start from 1): "), 0, 3);
            addComponent(getRefText1(), 1, 3);
        }

    }

    /*choose consistency*/
    class ConsistencyGrid extends GridFormat {
        ChoiceBox choiceColumn1;
        ChoiceBox choiceColumn2;

        ConsistencyGrid() {

            /*Initialize*/
            setFileButton(stage);
            setCbTitle();
            setHintTitle("Consistency");
            setPathText();
            this.choiceColumn1 = createChoiceBoxCols();
            this.choiceColumn2 = createChoiceBoxCols();
            setRefText1();
            setRefText2();

            /*Add to the grid*/
            addComponent(getCbTitle(), 0, 0);
            addComponent(getHintTitle(), 1, 0);
            addComponent(new Label("Reference"), 0, 1);
            addComponent(getPathText(), 1, 1);
            addComponent(getFileButton(), 2, 1);
            addComponent(new Label("examined column's\n" +
                    "Antecedent and Consequent"), 0, 2);
            addComponent(choiceColumn1, 1, 2);
            addComponent(choiceColumn2, 2, 2);
            addComponent(new Label("Reference's Antecedent\n" +
                    "and Consequent (number): "), 0, 3);
            addComponent(getRefText1(), 1, 3);
            addComponent(getRefText2(), 2, 3);
            addComponent(new Label("Follow the order of \n" +
                    "Antecedent->Consequent! "), 0, 4);
        }
    }

    /*choose Currency*/
    class CurrencyGrid extends GridFormat {
        ChoiceBox choiceColumnRow;

        CurrencyGrid() {
            /*Initialize*/
            this.choiceColumnRow = createChoiceBoxCols();
            setCbTitle();
            setHintTitle("Currency");
            setCbRow();
            setRefText1();
            setCbTable();

            /*Add to the grid*/
            addComponent(getCbTitle(), 0, 0);
            addComponent(getHintTitle(), 1, 0);
            addComponent(getCbRow(), 0, 1);
            addComponent(new Label("Row Level:"), 1, 1);
            addComponent(new Label("Primary Key:"), 2, 1);
            addComponent(choiceColumnRow, 3, 1);
            ;
            addComponent(getRefText1(), 4, 1);
            addComponent(getCbTable(), 0, 2);
            addComponent(new Label("Table level"), 1, 2);
        }
    }

    /*choose Completeness*/
    class CompletenessGrid extends GridFormat {
        ChoiceBox choiceColumnAttr;
        ChoiceBox choiceColumnRow;

        CompletenessGrid() {
            /*Initialize*/
            this.choiceColumnAttr = createChoiceBoxCols();
            this.choiceColumnRow = createChoiceBoxCols();
            setCbTitle();
            setHintTitle("Completeness");
            setCbRow();
            setRefText1();
            setCbAttribute();
            setCbTable();

            /*Add to the grid*/
            addComponent(getCbTitle(), 0, 0);
            addComponent(getHintTitle(), 1, 0);
            addComponent(getCbRow(), 0, 1);
            addComponent(new Label("Row Level:"), 1, 1);
            addComponent(new Label("Primary Key:"), 2, 1);
            addComponent(choiceColumnRow, 3, 1);
            ;
            addComponent(getRefText1(), 4, 1);
            addComponent(getCbAttribute(), 0, 2);
            addComponent(new Label("Attribute Level:"), 1, 2);
            addComponent(choiceColumnAttr, 2, 2);
            addComponent(getCbTable(), 0, 3);
            addComponent(new Label("Table level:"), 1, 3);
        }
    }

    /* Create Choice box which binds the colList*/
    private ChoiceBox createChoiceBoxCols() {
        ChoiceBox choiceBoxCols = new ChoiceBox();
        choiceBoxCols.itemsProperty().bind(this.colListProp);
        return choiceBoxCols;
    }


    private Button createButton() {
        Button submitButton = new Button("Submit");
        submitButton.setCursor(Cursor.CLOSED_HAND);
        submitButton.setPrefSize(80, 30);
        ResultService resultService = new ResultService();
        submitButton.setOnAction(event -> {

//                System.out.println(resultService.getState());
                    if (resultService.getState() == Worker.State.READY) {
                        ResultPage resultPage = new ResultPage();
                        resultService.start(); // start can only be called once
                        resultService.valueProperty().addListener(
                                (observable, oldValue, newValue) -> {
                                    // if no changed just for a moment, the new value will be null!
                                    if (newValue != null) {
                                        resultPage.setTextAreaForResult(newValue);
                                    }
                                }
                        );
                    } else {
                        resultService.restart();
                    }
                }
        );
        return submitButton;
    }

    class ResultService extends Service<String> {

        @Override
        public void start() {
            super.start();
        }

        @Override
        protected Task<String> createTask() {
            Task<String> task = new Task<String>() {
                @Override
                protected String call() throws Exception {
                    String summary = "";
                    if (accuracyGrid.getCbTitle().isSelected()) {
                        summary += getAccuracyResult(accuracyGrid);
                    }
                    if (consistencyGrid.getCbTitle().isSelected()) {
                        summary += getConsistencyResult(consistencyGrid);
                    }
                    if (currencyGrid.getCbTitle().isSelected()) {
                        summary += getCurrencyResult(currencyGrid);
                    }
                    if (completenessGrid.getCbTitle().isSelected()) {
                        summary += getCompletenessResult(completenessGrid);
                    }
                    return summary;
                }
            };
            return task;
        }
    }

    /*the following 4 functions connect to the backend code*/

    private String getAccuracyResult(AccuracyGrid accuracyGrid) {
        String result = "";
        try {
            Statement statement = conn.createStatement();
            String tableName = (String) choiceTable.getValue();
//                        System.out.println(tableName);
            String accuracyCol = (String) accuracyGrid.examinedCol.getValue();
            System.out.println(accuracyCol);

            String pathText = accuracyGrid.getPathText().getText();

            String refNum = accuracyGrid.getRefText1().getText().trim(); // trim to ensure safe input

            main.java.com.DBMS.Backend.Accuracy accuracy = new main.java.com.DBMS.Backend.Accuracy(tableName, accuracyCol, pathText, refNum
                    , statement);
            result += ("The Accuracy is: " + accuracy.calculate() + "\n");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String getConsistencyResult(ConsistencyGrid consistencyGrid) {
        String result = "";
        try {
            Statement statement = conn.createStatement();
            String tableName = (String) choiceTable.getValue();

            String consistencyCol1 = (String) consistencyGrid.choiceColumn1.getValue();
            System.out.println(consistencyCol1);

            String consistencyCol2 = (String) consistencyGrid.choiceColumn2.getValue();
            System.out.println(consistencyCol2);

            String pathText = consistencyGrid.getPathText().getText();

            String refNum1 = consistencyGrid.getRefText1().getText().trim(); // trim to ensure safe input
            String refNum2 = consistencyGrid.getRefText2().getText().trim(); // trim to ensure safe input

            Consistency consistency = new Consistency(tableName, consistencyCol1, consistencyCol2, pathText,
                    refNum1, refNum2, statement); //todo: too many parameters
            result += ("The Consistency is: " + consistency.calculation() + "\n");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }


    private String getCurrencyResult(CurrencyGrid currencyGrid) {
        String result = "";
        try {
            Statement statement = conn.createStatement();
            String tableName = (String) choiceTable.getValue();
//                        System.out.println(tableName);

            Currency currency = new Currency(tableName, statement);
            if (currencyGrid.getCbRow().isSelected()) {
                currency.setPrimaryKey((String) currencyGrid.choiceColumnRow.getValue());
                Currency.RowLevel rowLevel = currency.new RowLevel(
                        Long.parseLong(currencyGrid.getRefText1().getText()));
                result += ("The Currency for the row with primary key [" +
                        currencyGrid.choiceColumnRow.getValue() + "] = " +
                        currencyGrid.getRefText1().getText() + " " +
                        "is: " + rowLevel.calculate() + "\n");
            }

            if (currencyGrid.getCbTable().isSelected()) {
                Currency.TableLevel tableLevel = currency.new TableLevel();
                result += ("The relation Currency is: " + tableLevel.calculate() + "\n");

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String getCompletenessResult(CompletenessGrid completenessGrid) {
        String result = "";
        try {
            Statement statement = conn.createStatement();
            String tableName = (String) choiceTable.getValue();
//                        System.out.println(tableName);
            Completeness completeness = new Completeness(tableName, statement);

            if (completenessGrid.getCbRow().isSelected()) {

                Completeness.RowLevel rowLevel = completeness.
                        new RowLevel((String) completenessGrid.choiceColumnRow.getValue(),
                        completenessGrid.getRefText1().getText());
                result += ("The row level Completeness is: " + rowLevel.calculate() + "\n");

            }
            if (completenessGrid.getCbAttribute().isSelected()) {

                Completeness.AttributeLevel attributeLevel =
                        completeness.
                                new AttributeLevel((String) completenessGrid.choiceColumnAttr.getValue());
                result += ("The attribute level Completeness for " + completenessGrid.choiceColumnAttr.getValue()
                        + " is: " + attributeLevel.calculate() + "\n");
            }

            if (completenessGrid.getCbTable().isSelected()) {
                Completeness.TableLevel tableLevel = completeness.new TableLevel();
                result += ("The relation level completeness for is: " + tableLevel.calculate() + "\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    private BorderPane createBorderPane(GridPane tableGrid, GridPane metricsGrids, Button submitButton) {
        /*combine the table grid with the big grid above in the borderPane with position adjustments*/
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(tableGrid);
        borderPane.setMargin(tableGrid, new Insets(12, 12, 50, 12));
        borderPane.setCenter(metricsGrids);
        borderPane.setMargin(metricsGrids, new Insets(0, 0, 0, 30));
        borderPane.setBottom(submitButton);
        BorderPane.setMargin(submitButton, new Insets(0, 0, 30, 300));

        return borderPane;
    }

    private void createStage(BorderPane borderPane) {
        Scene scene = new Scene(borderPane);
        stage.setScene(scene);

        stage.setTitle("Metrics Selection");
        double sceneWidth = 850;
        double sceneHeight = 650;
        stage.setWidth(sceneWidth);
        stage.setHeight(sceneHeight);

        stage.setResizable(false); // set not to change window
        stage.initStyle(StageStyle.DECORATED);
        stage.show();
    }
}
