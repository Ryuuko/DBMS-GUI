package com.DBMS.Frontend;

import com.DBMS.Backend.*;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MetricsPage {
    private Connection conn;
    private SimpleListProperty<String> colListProp;
    private ChoiceBox choiceTable;
    private Stage stage;
    private HashMap<String, Float> nameMapMetric;
    private ScrollPane scrollPane;

    /*Create the Grid classes for the metrics*/
    private AccuracyMetricsGrid accuracyGrid;
    private ConsistencyMetricsGrid consistencyGrid;
    private CurrencyMetricsGrid currencyGrid;
    private CompletenessMetricsGrid completenessGrid;

    public MetricsPage(Connection connection) {

        /*initialize the member variables*/

        this.conn = connection;  // todo: remember to close the connection after the search, set in exit function
        this.stage = new Stage();
        ObservableList<String> colList = FXCollections.observableArrayList();
        this.colListProp = new SimpleListProperty<String>(colList);
        this.choiceTable = createChoiceBoxTable();
        this.nameMapMetric = new HashMap<String, Float>();

        /*put 4 metrics grids into the a big grid*/
        GridPane metricsGrids = createMetricsCollection();

        this.accuracyGrid = new AccuracyMetricsGrid();
        this.consistencyGrid = new ConsistencyMetricsGrid();
        this.currencyGrid = new CurrencyMetricsGrid();
        this.completenessGrid = new CompletenessMetricsGrid();

        metricsGrids.add(accuracyGrid.getGr(), 0, 0);
        metricsGrids.add(consistencyGrid.getGr(), 1, 0);
        metricsGrids.add(currencyGrid.getGr(), 0, 1);
        metricsGrids.add(completenessGrid.getGr(), 1, 1);

        /*combine the table grid with the big grid above in the borderPane with position adjustments*/

        GridPane tableGrid = createTitleGird();

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

    private GridPane createTitleGird() {
        GridPane gr = new GridPane();
        Label hint = new Label();
        hint.setText("First of all, choose\n the examined table:\n" +
                "If possible, \n name columns without dots");

        this.scrollPane = createScrollPane();

        gr.add(hint, 0, 1);
        gr.add(choiceTable, 1, 1);
        gr.add(this.scrollPane, 2, 1);
        gr.setHgap(50);
        return gr;
    }

    private ScrollPane createScrollPane() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPrefHeight(120);
        scrollPane.setPrefWidth(500);
        return scrollPane;
    }

    private GridPane createMetricsCollection() {
        GridPane metricsGrids = new GridPane();
        metricsGrids.setHgap(50);
        metricsGrids.setVgap(50);
        return metricsGrids;
    }

    /*The following the 4 subclasses inherit the similar grid parent class, four of them are the metrics grid*/

    /*choose accuracy*/
    class AccuracyMetricsGrid extends MetricsGridFormat {
        ChoiceBox examinedCol;
        TextField refText;
        CheckBox traditionalCheckBox;
        CheckBox levenshteinCheckBox;

        AccuracyMetricsGrid() {

            /*initialize*/
            this.examinedCol = createChoiceBoxCols(colListProp);
            this.refText = createRefText();
            this.traditionalCheckBox = createNormalCheckBox();
            this.levenshteinCheckBox = createNormalCheckBox();

            setFileButton(stage);
            setHintTitle("Accuracy");
            setCbTitle();
            setPathText();


            /*Add to the grid*/
            addComponent(getCbTitle(), 0, 0);
            addComponent(getHintTitle(), 1, 0);

            addComponent(this.traditionalCheckBox, 0, 1);
            addComponent(new Label("Traditional"), 1, 1);
            addComponent(this.levenshteinCheckBox, 2, 1);
            addComponent(new Label("Levenshtein"), 3, 1);

            addComponent(new Label("Reference"), 0, 2);
            addComponent(getPathText(), 1, 2);
            addComponent(getFileButton(), 2, 2);
            addComponent(new Label("examined column"), 0, 3);
            addComponent(this.examinedCol, 1, 3);
            addComponent(new Label("Reference's column \n (start from 1): "), 0, 4);
            addComponent(refText, 1, 4);
        }

    }

    /*choose consistency*/
    class ConsistencyMetricsGrid extends MetricsGridFormat {
        ChoiceBox choiceColumn1;
        ChoiceBox choiceColumn2;
        TextField refText1;
        TextField refText2;

        ConsistencyMetricsGrid() {
            /*Initialize*/
            this.choiceColumn1 = createChoiceBoxCols(colListProp);
            this.choiceColumn2 = createChoiceBoxCols(colListProp);
            this.refText1 = createRefText();
            this.refText2 = createRefText();

            setFileButton(stage);
            setCbTitle();
            setHintTitle("Consistency");
            setPathText();

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
            addComponent(refText1, 1, 3);
            addComponent(refText2, 2, 3);
            addComponent(new Label("Follow the order of \n" +
                    "Antecedent->Consequent! "), 0, 4);
        }
    }

    /*choose Currency*/
    class CurrencyMetricsGrid extends MetricsGridFormat {
        ChoiceBox choiceColumnRow;
        CheckBox rowLevelCheckBox;
        CheckBox tableLevelCheckBox;
        TextField refText;


        CurrencyMetricsGrid() {
            /*Initialize*/
            this.choiceColumnRow = createChoiceBoxCols(colListProp);
            this.rowLevelCheckBox = createNormalCheckBox();
            this.tableLevelCheckBox = createNormalCheckBox();
            setCbTitle();
            setHintTitle("Currency");

            this.refText = createRefText();

            /*Add to the grid*/
            addComponent(getCbTitle(), 0, 0);
            addComponent(getHintTitle(), 1, 0);
            addComponent(rowLevelCheckBox, 0, 1);
            addComponent(new Label("Row Level:"), 1, 1);
            addComponent(new Label("Primary Key:"), 2, 1);
            addComponent(choiceColumnRow, 3, 1);
            addComponent(refText, 4, 1);
            addComponent(tableLevelCheckBox, 0, 2);
            addComponent(new Label("Table level"), 1, 2);
        }
    }

    /*choose Completeness*/
    class CompletenessMetricsGrid extends MetricsGridFormat {
        ChoiceBox choiceColumnAttr;
        ChoiceBox choiceColumnRow;
        CheckBox rowLevelCheckBox;
        CheckBox attributeLevelCheckBox;
        CheckBox tableLevelCheckBox;

        TextField refText;

        CompletenessMetricsGrid() {
            /*Initialize*/
            this.choiceColumnAttr = createChoiceBoxCols(colListProp);
            this.choiceColumnRow = createChoiceBoxCols(colListProp);
            this.refText = createRefText();
            this.rowLevelCheckBox = createNormalCheckBox();
            this.attributeLevelCheckBox = createNormalCheckBox();
            this.tableLevelCheckBox = createNormalCheckBox();

            setCbTitle();
            setHintTitle("Completeness");

            /*Add to the grid*/
            addComponent(getCbTitle(), 0, 0);
            addComponent(getHintTitle(), 1, 0);
            addComponent(this.rowLevelCheckBox, 0, 1);
            addComponent(new Label("Row Level:"), 1, 1);
            addComponent(new Label("Primary Key:"), 2, 1);
            addComponent(choiceColumnRow, 3, 1);
            addComponent(this.refText, 4, 1);
            addComponent(this.attributeLevelCheckBox, 0, 2);
            addComponent(new Label("Attribute Level:"), 1, 2);
            addComponent(choiceColumnAttr, 2, 2);
            addComponent(this.tableLevelCheckBox, 0, 3);
            addComponent(new Label("Table level:"), 1, 3);
        }
    }

    private Button createButton() {
        Button submitButton = new Button("Submit");
        submitButton.setCursor(Cursor.CLOSED_HAND);
        submitButton.setPrefSize(80, 30);
        submitButton.setOnAction(event -> {

                    nameMapMetric.clear();
                    getResultContent();
                    BarChart<String, Number> barChart = createChart();
                    VBox verticalContainer = new VBox();
                    verticalContainer.setPrefHeight(200);
                    verticalContainer.getChildren().add(barChart);
                    this.scrollPane.setContent(verticalContainer);

                }
        );
        return submitButton;
    }

    private BarChart<String, Number> createChart() {

        CategoryAxis x = new CategoryAxis();
//        x.setStartMargin(50);
//        x.setEndMargin(50);
        x.setTickLength(5);
//        x.setTickLabelRotation(90);
        x.setLabel("Metrics");

        NumberAxis y = new NumberAxis(0, 100, 20);
        y.setSide(Side.LEFT);
        y.setLabel("Score (in %)");

        XYChart.Series<String, Number> xy = new XYChart.Series<String, Number>();
        xy.setName("Metrics");

        Iterator iterator = nameMapMetric.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();
            XYChart.Data<String, Number> data =
                    new XYChart.Data<String, Number>((String) pair.getKey(), (float) pair.getValue() * 100);
            xy.getData().add(data);
        }

        xy.getData().forEach(System.out::println);
        BarChart<String, Number> barChart = new BarChart<String, Number>(x, y);
        barChart.getData().add(xy);
//        barChart.setBarGap(5);
//        barChart.setPrefWidth(500);

        return barChart;
    }

    private void getResultContent() {

        if (accuracyGrid.getCbTitle().isSelected()) {
            getAccuracyResult();
        }
        if (consistencyGrid.getCbTitle().isSelected()) {
            getConsistencyResult();
        }
        if (currencyGrid.getCbTitle().isSelected()) {
            getCurrencyResult();
        }
        if (completenessGrid.getCbTitle().isSelected()) {
            getCompletenessResult();
        }
    }


    /*the following 4 functions connect to the backend code*/


    private void getAccuracyResult() {
        try {

            Statement statement = conn.createStatement();
            String tableName = (String) choiceTable.getValue();
            String accuracyCol = (String) accuracyGrid.examinedCol.getValue();
            System.out.println(accuracyCol);

            String pathText = accuracyGrid.getPathText().getText();

            String refNum = accuracyGrid.refText.getText().trim(); // trim to ensure safe input

            Accuracy accuracy = new Accuracy(tableName, accuracyCol, pathText, refNum
                    , statement);

            if (accuracyGrid.traditionalCheckBox.isSelected()) {
                Accuracy.Traditional traditional = accuracy.new Traditional();
                nameMapMetric.put("Traditional Accuracy", traditional.calculate());
            }
            if (accuracyGrid.levenshteinCheckBox.isSelected()) {
                Accuracy.Levenshtein levenshtein = accuracy.new Levenshtein();
                nameMapMetric.put("Levenshtein Accuracy", levenshtein.calculate());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void getConsistencyResult() {
        try {
            Statement statement = conn.createStatement();
            String tableName = (String) choiceTable.getValue();

            String consistencyCol1 = (String) consistencyGrid.choiceColumn1.getValue();
            System.out.println(consistencyCol1);

            String consistencyCol2 = (String) consistencyGrid.choiceColumn2.getValue();
            System.out.println(consistencyCol2);

            String pathText = consistencyGrid.getPathText().getText();

            String refNum1 = consistencyGrid.refText1.getText().trim(); // trim to ensure safe input
            String refNum2 = consistencyGrid.refText2.getText().trim(); // trim to ensure safe input

            Consistency consistency = new Consistency(tableName, consistencyCol1, consistencyCol2, pathText,
                    refNum1, refNum2, statement); //todo: too many parameters
            nameMapMetric.put("Consistency", (float) consistency.calculation());
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    private void getCurrencyResult() {
        try {
            Statement statement = conn.createStatement();
            String tableName = (String) choiceTable.getValue();

            Currency currency = new Currency(tableName, statement);

            if (currencyGrid.choiceColumnRow.getValue() != null) {
                currency.setPrimaryKey((String) currencyGrid.choiceColumnRow.getValue());
            } else {
                currency.setPrimaryKey(colListProp.getValue().get(0)); // first column is usually the primary key
            }

            if (currencyGrid.rowLevelCheckBox.isSelected()) {

                Currency.RowLevel rowLevel = currency.new RowLevel(
                        Long.parseLong(currencyGrid.refText.getText()));

                nameMapMetric.put("Currency - for primary key " + currencyGrid.choiceColumnRow.getValue() +
                                " = " + currencyGrid.refText.getText()
                        , (float) rowLevel.calculate());

            }

            if (currencyGrid.tableLevelCheckBox.isSelected()) {
                Currency.TableLevel tableLevel = currency.new TableLevel();
                nameMapMetric.put("Currency - Table Level", (float) tableLevel.calculate());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void getCompletenessResult() {
        try {
            Statement statement = conn.createStatement();
            String tableName = (String) choiceTable.getValue();
            Completeness completeness = new Completeness(tableName, statement);

            if (completenessGrid.rowLevelCheckBox.isSelected()) {

                Completeness.RowLevel rowLevel = completeness.
                        new RowLevel((String) completenessGrid.choiceColumnRow.getValue(),
                        completenessGrid.refText.getText());

                nameMapMetric.put(completenessGrid.choiceColumnRow.getValue() +
                                " = " + completenessGrid.refText.getText()
                        , rowLevel.calculate());
            }
            if (completenessGrid.attributeLevelCheckBox.isSelected()) {

                Completeness.AttributeLevel attributeLevel =
                        completeness.new AttributeLevel((String) completenessGrid.choiceColumnAttr.getValue());

                nameMapMetric.put("Completeness - for attribute " + completenessGrid.choiceColumnAttr.getValue()
                        , attributeLevel.calculate());
            }

            if (completenessGrid.tableLevelCheckBox.isSelected()) {
                Completeness.TableLevel tableLevel = completeness.new TableLevel();
                nameMapMetric.put("Completeness - Table Level", tableLevel.calculate());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        double sceneHeight = 700;
        stage.setWidth(sceneWidth);
        stage.setHeight(sceneHeight);

        stage.setResizable(false); // set not to change window
        stage.initStyle(StageStyle.DECORATED);
        stage.show();
    }
}
