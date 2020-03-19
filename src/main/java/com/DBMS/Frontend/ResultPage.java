package com.DBMS.Frontend;

import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ResultPage {

    TextArea textAreaForResult;

    public ResultPage() {
        this.textAreaForResult = createReportText();
        createStage(textAreaForResult);
    }

    private TextArea createReportText() {
        return new TextArea();
    }

    private void createStage(TextArea textArea) {
        Scene scene = new Scene(textArea);
        Stage stage = new Stage();
        stage.setScene(scene);

        stage.setTitle("Result: ");
        double sceneWidth = 550;
        double sceneHeight = 650;
        stage.setWidth(sceneWidth);
        stage.setHeight(sceneHeight);

        stage.setResizable(false); // set not to change window
        stage.initStyle(StageStyle.DECORATED);
        stage.show();
    }

    public void setTextAreaForResult(String newResult) {
        this.textAreaForResult.setText(newResult);
    }
}
