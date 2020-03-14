package com.DBMS.Frontend;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;


public class GridFormat {

    private GridPane gr;
    private Button fileButton;
    private Label hintTitle;
    private CheckBox cbTitle;
    private CheckBox cbRow;
    private CheckBox cbAttribute;
    private CheckBox cbTable;
    private TextField pathText;
    private TextField refText1;
    private TextField refText2;

    public GridFormat() {
        /*construct some common value of the grid pane in order to standardize*/
        this.gr = new GridPane();
        gr.setAlignment(Pos.CENTER_LEFT);
        gr.setHgap(10);
        gr.setVgap(15);
    }

    /*Add node into the GridPane*/
    public void addComponent(Node node, int col, int row) {
        makeNodeTransparent(node); // disable as default
        this.gr.add(node, col, row);
    }

    private void makeNodeTransparent(Node node) {
        /*disable all components except the title and its checkbox*/
        if (node.getUserData() == null) {
            node.setMouseTransparent(true); // if true, transparent to mouse events
            node.setOpacity(0.5);
        }
    }

    /*getter and setter for member variables. Set and get particular components when required*/

    public GridPane getGr() {
        return gr;
    }

    public void setHintTitle(String title) {
        this.hintTitle = new Label(title);
        this.hintTitle.setUserData("title");
    }

    public Label getHintTitle() {
        return hintTitle;
    }

    public void setPathText() {
        this.pathText = new TextField();
        pathText.setMaxHeight(5);
    }

    public void setRefText1() {
        this.refText1 = new TextField(); // todo: only number is allowed
        refText1.setMaxSize(30, 30);
    }

    public void setRefText2() {
        this.refText2 = new TextField(); // todo: only number is allowed
        refText2.setMaxSize(30, 30);
    }

    public TextField getPathText() {
        return pathText;
    }

    public TextField getRefText1() {
        return refText1;
    }

    public TextField getRefText2() {
        return refText2;
    }

    public Button getFileButton() {
        return fileButton;
    }

    /*click the button will trigger the file navigator!*/
    public void setFileButton(Stage s) {
        this.fileButton = new Button("...");
        fileButton.setMaxSize(10, 10);
        fileButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                File file = fileChooser.showOpenDialog(s);
                if (file != null) {
                    pathText.setText(file.getAbsolutePath());
                }
            }
        });
    }

    public CheckBox getCbTitle() {
        return cbTitle;
    }

    /*the checkbox with title should control if the grid components display*/
    public void setCbTitle() {
        this.cbTitle = new CheckBox();
        String keyword = "Cbtitle";
        cbTitle.setUserData(keyword);
        cbTitle.setSelected(false); // ensure it's unselected
        cbTitle.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                /*if there is any change, eg. select the checkbox*/
                if (newValue) {
                    enableComponents(); // if selected, enable all the node components
                } else {
                    disableComponents();
                }
            }
        });

    }

    /*enable all the components in the grid*/
    private void enableComponents() {
        gr.getChildren().forEach(node -> {
            node.setMouseTransparent(false);
            node.setOpacity(1);
//            System.out.println("Enabler Processing: " + node.toString());
        });
    }

    /*disable all the components in the grid, except the title checkbox*/
    private void disableComponents() {
        gr.getChildren().forEach(this::makeNodeTransparent); //
    }

    public CheckBox getCbRow() {
        return cbRow;
    }

    public void setCbRow() {
        this.cbRow = new CheckBox();
    }

    public CheckBox getCbAttribute() {
        return cbAttribute;
    }

    public void setCbAttribute() {
        this.cbAttribute = new CheckBox();
    }

    public CheckBox getCbTable() {
        return cbTable;
    }

    public void setCbTable() {
        this.cbTable = new CheckBox();
    }

}
