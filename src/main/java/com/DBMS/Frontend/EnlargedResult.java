/*Open a bigger screen to show the graph*/

package com.DBMS.Frontend;

import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class EnlargedResult {

    public EnlargedResult(ScrollPane originPane, String titleName) {
        final Stage stage = new Stage();
        ScrollPane largeScrollPane = new ScrollPane();
        largeScrollPane.setContent(originPane.getContent());

        Scene scene = new Scene(largeScrollPane);
        stage.setScene(scene);
        stage.setTitle(titleName + "'s result");
        stage.setHeight(450);
        stage.setWidth(500);
        stage.initStyle(StageStyle.DECORATED);

        stage.show();

    }
}
