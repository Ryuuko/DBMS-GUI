package com.DBMS.Frontend;

import com.DBMS.Backend.DataGetter.ColumnsListGetter;
import com.DBMS.Backend.DataGetter.TableListGetter;
import com.DBMS.Backend.Metrics.Accuracy;
import com.DBMS.Backend.Metrics.Completeness;
import com.DBMS.Backend.Metrics.Consistency;
import com.DBMS.Backend.Metrics.Currency;
import com.DBMS.Backend.ObjectClass.MetricsParameter;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.*;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/*Via this class user interact with the program in order to get the desired metrics*/

public class MetricsPage {
    private Connection conn;
    private SimpleListProperty<String> colListProp;
    private ChoiceBox choiceTable;
    private Stage stage;
    private HashMap<String, Float> nameMapMetric;
    private ScrollPane scrollPane;

    // Two Layers of Gridpanes in the stage. There is a submit buttons below
    private GridPane tableGrid;
    private GridPane metricsGrids;
    private Button submitButton;


    // Create the Grid classes for the metrics
    private AccuracyMetricsGrid accuracyGrid;
    private ConsistencyMetricsGrid consistencyGrid;
    private CurrencyMetricsGrid currencyGrid;
    private CompletenessMetricsGrid completenessGrid;

    public MetricsPage(Connection connection) {

        //initialize the member variables
        this.conn = connection;
        this.stage = new Stage();
        ObservableList<String> colList = FXCollections.observableArrayList();
        this.colListProp = new SimpleListProperty<String>(colList);
        this.choiceTable = createChoiceBoxTable();
        this.nameMapMetric = new HashMap<String, Float>();

        // put 4 metrics grids into the a big grid
        this.metricsGrids = createMetricsCollection();

        this.accuracyGrid = new AccuracyMetricsGrid();
        this.consistencyGrid = new ConsistencyMetricsGrid();
        this.currencyGrid = new CurrencyMetricsGrid();
        this.completenessGrid = new CompletenessMetricsGrid();

        this.metricsGrids.add(accuracyGrid.getGr(), 0, 0);
        this.metricsGrids.add(consistencyGrid.getGr(), 1, 0);
        this.metricsGrids.add(currencyGrid.getGr(), 0, 1);
        this.metricsGrids.add(completenessGrid.getGr(), 1, 1);

        // combine the table grid with the big grid above in the borderPane with position adjustments
        this.tableGrid = createTitleGird();

        // create button to connect the backend
        this.submitButton = createSubmitButton();

        BorderPane borderPane = createBorderPane();

        // finally put all stuffs into the scene
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
                    // We build the name-list to reduce unnecessary visit to the database connection
                    if (!tableColumnMap.containsKey(newValue)) {
                        // via the connection to ask the relevant columns

                        ColumnsListGetter columnsListGetter = new ColumnsListGetter(conn, newValue);
                        ObservableList<String> newObsList =
                                FXCollections.observableArrayList(columnsListGetter.getColumns());
                        tableColumnMap.put(newValue, newObsList);
                    }

                    // the choice box is bound with colListProp, when we change colListProp
                    // the content in the choice box will be also changed
                    colListProp.set(tableColumnMap.get(newValue));

                });
    }

    private GridPane createMetricsCollection() {
        GridPane metricsGrids = new GridPane();
        metricsGrids.setHgap(50);
        metricsGrids.setVgap(50);
        return metricsGrids;
    }


    // The following the 4 subclasses inherit the similar grid parent class, four of them are the metrics grid
    class AccuracyMetricsGrid extends MetricsGridFormat {

        ChoiceBox examinedCol;
        TextField refText;
        CheckBox traditionalCheckBox;
        CheckBox levenshteinCheckBox;
        CheckBox skipHeader;

        AccuracyMetricsGrid() {

            this.examinedCol = createChoiceBoxCols(colListProp);
            this.refText = createRefText();
            this.traditionalCheckBox = new CheckBox();
            this.levenshteinCheckBox = new CheckBox();
            this.skipHeader = new CheckBox();

            setFileButton(stage);
            setHintTitle("Accuracy");
            setCbTitle();
            setPathText();

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

            addComponent(this.skipHeader, 0, 5);
            addComponent(new Label("skip the header of csv?"), 1, 5);
        }


    }

    class ConsistencyMetricsGrid extends MetricsGridFormat {

        ChoiceBox choiceColumn1;
        ChoiceBox choiceColumn2;
        TextField refText1;
        TextField refText2;
        CheckBox skipHeader;

        ConsistencyMetricsGrid() {
            this.choiceColumn1 = createChoiceBoxCols(colListProp);
            this.choiceColumn2 = createChoiceBoxCols(colListProp);
            this.refText1 = createRefText();
            this.refText2 = createRefText();
            this.skipHeader = new CheckBox();

            setFileButton(stage);
            setCbTitle();
            setHintTitle("Consistency");
            setPathText();

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
                    "and Consequent \n (start from 1): "), 0, 3);
            addComponent(refText1, 1, 3);
            addComponent(refText2, 2, 3);
            addComponent(new Label("Follow the order of \n" +
                    "Antecedent->Consequent! "), 0, 4);
            addComponent(this.skipHeader, 0, 5);
            addComponent(new Label("skip the header of csv?"), 1, 5);
        }

    }

    class CurrencyMetricsGrid extends MetricsGridFormat {

        ChoiceBox choiceColumnRow;
        CheckBox rowLevelCheckBox;
        CheckBox tableLevelCheckBox;
        TextField refText;

        CurrencyMetricsGrid() {
            this.choiceColumnRow = createChoiceBoxCols(colListProp);
            this.rowLevelCheckBox = new CheckBox();
            this.tableLevelCheckBox = new CheckBox();
            setCbTitle();
            setHintTitle("Currency");

            this.refText = createRefText();

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

    class CompletenessMetricsGrid extends MetricsGridFormat {
        ChoiceBox choiceColumnAttr;

        ChoiceBox choiceColumnRow;
        CheckBox rowLevelCheckBox;
        CheckBox attributeLevelCheckBox;
        CheckBox tableLevelCheckBox;
        TextField refText;

        CompletenessMetricsGrid() {

            this.choiceColumnAttr = createChoiceBoxCols(colListProp);
            this.choiceColumnRow = createChoiceBoxCols(colListProp);
            this.refText = createRefText();
            this.rowLevelCheckBox = new CheckBox();
            this.attributeLevelCheckBox = new CheckBox();
            this.tableLevelCheckBox = new CheckBox();

            setCbTitle();
            setHintTitle("Completeness");

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

    private GridPane createTitleGird() {
        GridPane gr = new GridPane();
        Label hint = new Label();
        hint.setText("First of all, choose\n the examined table:\n" +
                "No dot for column \n name!");

        this.scrollPane = createScrollPane();

        gr.add(hint, 0, 0);
        gr.add(choiceTable, 1, 0);
        gr.add(this.scrollPane, 2, 0);
        gr.add(createEnlargeButton(), 2, 1);
        gr.setHgap(50);
        return gr;
    }

    private ScrollPane createScrollPane() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPrefHeight(130);
        scrollPane.setPrefWidth(500);
        scrollPane.setContent(new Label("Your result will be shown in this box~!"));
        return scrollPane;
    }

    private Button createEnlargeButton() {
        Button button = new Button("Enlarge and keep the result");
        button.setOnAction(event -> {
            new EnlargedResult(this.scrollPane);
        });
        return button;
    }

    private Button createSubmitButton() {
        Button submitButton = new Button("Submit");
        submitButton.setCursor(Cursor.CLOSED_HAND);
        submitButton.setPrefSize(80, 30);
        ResultService resultService = createResultService();
        submitButton.setOnAction(event -> {

                    // the UX thread which display while calculating
                    this.scrollPane.setContent(new Label("Loading"));

                    if (resultService.getState() == Worker.State.READY) {
                        resultService.start();
                    } else {
                        // we click the button more than once, use restart instead
                        resultService.restart();
                    }
                }
        );

        return submitButton;
    }

    private ResultService createResultService() {
        ResultService resultService = new ResultService();
        resultService.setOnSucceeded((event) -> {
            // the thread in the service to calculate
            this.scrollPane.setContent(resultService.getValue());
        });
        return resultService;
    }

    class ResultService extends Service<BarChart> {

        @Override
        protected Task<BarChart> createTask() {
            Task<BarChart> task = new Task<BarChart>() {
                @Override
                protected BarChart call() throws Exception {
                    return createDataChart();
                }
            };
            return task;
        }
    }

    private BarChart<Number, String> createDataChart() {

        nameMapMetric.clear();
        getResultContent();
        return drawResult();
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


    // the following 4 functions connect to the backend code

    private void getAccuracyResult() {
        String tableName = (String) choiceTable.getValue();
        String accuracyCol = (String) accuracyGrid.examinedCol.getValue();
        String pathText = accuracyGrid.getPathText().getText();
        String refNum = accuracyGrid.refText.getText().trim(); // trim to ensure safe input
        boolean skipOption = accuracyGrid.skipHeader.isSelected();


        MetricsParameter accuracyInput = new MetricsParameter();
        accuracyInput.setTableName(tableName);
        accuracyInput.setColumns1(accuracyCol);
        accuracyInput.setPath(pathText);
        accuracyInput.setRef1(refNum);
        accuracyInput.setConnection(conn);
        accuracyInput.setSkip(skipOption);

        Accuracy accuracy = new Accuracy(accuracyInput);

        if (accuracyGrid.traditionalCheckBox.isSelected()) {
            Accuracy.Traditional traditional = accuracy.new Traditional();
            nameMapMetric.put("Accuracy - Traditional", traditional.calculate());
        }
        if (accuracyGrid.levenshteinCheckBox.isSelected()) {
            Accuracy.Levenshtein levenshtein = accuracy.new Levenshtein();
            nameMapMetric.put("Accuracy - Levenshtein", levenshtein.calculate());
        }
    }

    private void getConsistencyResult() {

        String tableName = (String) choiceTable.getValue();
        String consistencyCol1 = (String) consistencyGrid.choiceColumn1.getValue();
        String consistencyCol2 = (String) consistencyGrid.choiceColumn2.getValue();
        String pathText = consistencyGrid.getPathText().getText();
        String refNum1 = consistencyGrid.refText1.getText().trim(); // trim to ensure safe input
        String refNum2 = consistencyGrid.refText2.getText().trim(); // trim to ensure safe input
        boolean skipOption = consistencyGrid.skipHeader.isSelected();

        MetricsParameter consistencyInput = new MetricsParameter();
        consistencyInput.setConnection(conn);
        consistencyInput.setTableName(tableName);
        consistencyInput.setColumns1(consistencyCol1);
        consistencyInput.setColumns2(consistencyCol2);
        consistencyInput.setPath(pathText);
        consistencyInput.setRef1(refNum1);
        consistencyInput.setRef2(refNum2);
        consistencyInput.setSkip(skipOption);

        Consistency consistency = new Consistency(consistencyInput);
        nameMapMetric.put("Consistency", (float) consistency.calculate());

    }


    private void getCurrencyResult() {

        String tableName = (String) choiceTable.getValue();
        MetricsParameter currencyInput = new MetricsParameter();
        currencyInput.setConnection(conn);
        currencyInput.setTableName(tableName);

        Currency currency = new Currency(currencyInput);

        if (currencyGrid.choiceColumnRow.getValue() != null) {
            currency.setPrimaryKey((String) currencyGrid.choiceColumnRow.getValue());
        } else {
            // first column is usually the primary key
            currency.setPrimaryKey(colListProp.getValue().get(0));
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
    }

    private void getCompletenessResult() {
        String tableName = (String) choiceTable.getValue();
        MetricsParameter completenessInput = new MetricsParameter();
        completenessInput.setConnection(conn);
        completenessInput.setTableName(tableName);

        Completeness completeness = new Completeness(completenessInput);

        if (completenessGrid.rowLevelCheckBox.isSelected()) {

            Completeness.RowLevel rowLevel = completeness.
                    new RowLevel((String) completenessGrid.choiceColumnRow.getValue(),
                    completenessGrid.refText.getText());

            nameMapMetric.put(
                    "Completeness - Row Level: " +
                            completenessGrid.choiceColumnRow.getValue() +
                            " = " + completenessGrid.refText.getText()
                    , rowLevel.calculate());
        }
        if (completenessGrid.attributeLevelCheckBox.isSelected()) {

            Completeness.AttributeLevel attributeLevel =
                    completeness.new AttributeLevel((String) completenessGrid.choiceColumnAttr.getValue());

            nameMapMetric.put("Completeness - Attribute Level: " +
                            completenessGrid.choiceColumnAttr.getValue()
                    , attributeLevel.calculate());
        }

        if (completenessGrid.tableLevelCheckBox.isSelected()) {
            Completeness.TableLevel tableLevel = completeness.new TableLevel();
            nameMapMetric.put("Completeness - Table Level", tableLevel.calculate());
        }
    }

    private BarChart<Number, String> drawResult() {

        CategoryAxis x = new CategoryAxis();
        x.setTickLength(5);
        x.setLabel("Metrics");

        NumberAxis y = new NumberAxis(0, 100, 20);
        y.setSide(Side.TOP);
        y.setLabel("Score (in %)");

        HashMap<String, XYChart.Series<Number, String>> chartSeriesList = new HashMap<>();
        Iterator iterator = nameMapMetric.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();

            // "-" delimiter is related with how getResult(Metrics) Methods put the key into the HashMap
            String metricName = pair.getKey().toString().split(" - ")[0];

            if (!chartSeriesList.containsKey(metricName)) {
                XYChart.Series<Number, String> newSeries = new XYChart.Series<Number, String>();
                newSeries.setName(metricName);
                chartSeriesList.put(metricName, newSeries);
            }

            XYChart.Data<Number, String> data =
                    new XYChart.Data<Number, String>((float) pair.getValue() * 100,
                            pair.getKey().toString());

            /*data.nodeProperty().addListener seems to be used, detecting whether the node is null,
            as long as we want to change node without using setnode. And the node must be null before
            we actually add the data to the chart.*/

            data.nodeProperty().addListener(new ChangeListener<Node>() {
                @Override
                public void changed(ObservableValue<? extends Node> ov, Node oldNode, final Node node) {
                    if (node != null) {
                        displayLabelForData(data);
                    }
                }
            });

            chartSeriesList.get(metricName).getData().add(data);

        }


        BarChart<Number, String> barChart = new BarChart<Number, String>(y, x);
        barChart.getData().addAll(chartSeriesList.values());
        barChart.setBarGap(0.0);
        barChart.setCategoryGap(10);
        barChart.setPrefWidth(450);
        barChart.setPrefHeight(50 * nameMapMetric.size());

        return barChart;
    }

    // places a text label with a bar's value above a bar node for a given XYChart.Data

    /* This method is imitated according to the answer of
    https://stackoverflow.com/questions/15237192/how-to-display-bar-value-on-top-of-bar-javafx

    The mechanism of the change listener of parentProperty() and boundsInParentProperty()
     is quite unclear to the author. It is known that The content of changelistener of parentProperty
     will be triggered when getData().addAll, aka. adding the data to the chart.*/

    private void displayLabelForData(XYChart.Data<Number, String> data) {
        final Node node = data.getNode();
        final Text dataText = new Text(data.getXValue().intValue() + "%");
        dataText.setFont(new Font(9));
        node.parentProperty().addListener(new ChangeListener<Parent>() {
            @Override
            public void changed(ObservableValue<? extends Parent> ov, Parent oldParent, Parent parent) {
                // a Group will be the smallest rectangle containing the bounds of all the child nodes
                Group parentGroup = (Group) parent;
                parentGroup.getChildren().add(dataText);
            }
        });

        node.boundsInParentProperty().addListener(new ChangeListener<Bounds>() {
            @Override
            public void changed(ObservableValue<? extends Bounds> ov, Bounds oldBounds, Bounds bounds) {
                dataText.setLayoutX(
                        Math.round(bounds.getMaxX()) + dataText.prefWidth(-1) / 5
                );
                dataText.setLayoutY(
                        Math.round(
                                bounds.getMinY() + bounds.getHeight() / 2
                        )
                );
            }
        });
    }

    private BorderPane createBorderPane() {
        // combine the table grid with the big grid above in the borderPane with position adjustments
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(tableGrid);
        borderPane.setMargin(tableGrid, new Insets(12, 12, 50, 12));
        borderPane.setCenter(metricsGrids);
        borderPane.setMargin(metricsGrids, new Insets(0, 0, 0, 30));

        borderPane.setBottom(submitButton);

        return borderPane;
    }

    private void createStage(BorderPane borderPane) {
        Scene scene = new Scene(borderPane);
        stage.setScene(scene);

        stage.setTitle("Metrics Selection");
        double sceneWidth = 850;
        double sceneHeight = 750;
        stage.setWidth(sceneWidth);
        stage.setHeight(sceneHeight);

        stage.setResizable(false); // set not to change window
        stage.initStyle(StageStyle.DECORATED);

        // dynamically set the button centrally
        double buttonStart = stage.getWidth() / 2 - submitButton.getPrefWidth();
        BorderPane.setMargin(submitButton, new Insets(0, 0, 20, buttonStart));
        stage.show();
    }
}
