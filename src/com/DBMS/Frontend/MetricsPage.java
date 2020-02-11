package com.DBMS.Frontend;

import com.DBMS.Backend.ColumnsGetter;
import com.DBMS.Backend.TablesGetter;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.Connection;

public class MetricsPage {
    private final Stage s = new Stage(); // initialize the stage after Instantiation

    public MetricsPage(Connection connection) {

        // todo: remember to close the connection after the search, set in exit function

        TablesGetter tablesGetter = new TablesGetter(connection);
        ObservableList<String> tableList = FXCollections.observableList(tablesGetter.getTableList());

        ChoiceBox cbTable = new ChoiceBox(tableList);
        ChoiceBox cbColumn = new ChoiceBox();

        /*Change the columns list if change*/
        cbTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue observable, String oldValue, String newValue) {
                ColumnsGetter columnsGetter = new ColumnsGetter(connection, newValue);
                ObservableList<String> colList = FXCollections.observableList(columnsGetter.getColumns());
                cbColumn.setItems(colList); // update the value in the column choice box
            }
        });

        Label tableLabel = new Label("Choose Table:");
        Label columnLabel = new Label("Choose Column:");
        GridPane gr = new GridPane();
        gr.setStyle("-fx-background-color:#FFF5EE");
        gr.setAlignment(Pos.TOP_LEFT);
        gr.setHgap(10);
        gr.setVgap(15);
        gr.add(tableLabel, 0, 0);
        gr.add(columnLabel, 1, 0);
        gr.add(cbTable, 0, 1);
        gr.add(cbColumn, 1, 1);

        double sceneWidth = 500;
        double sceneHeight = 500;


        Scene scene = new Scene(gr);
        s.setScene(scene);

        s.setTitle("Metrics Selection");
        s.getIcons().add(new Image("com\\DBMS\\Frontend\\icon.png"));
        s.setWidth(sceneWidth);
        s.setHeight(sceneHeight);

        s.setResizable(false); // set not to change window
        s.initStyle(StageStyle.DECORATED);
        s.show();
    }
}
