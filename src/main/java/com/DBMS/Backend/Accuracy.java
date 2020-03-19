package main.java.com.DBMS.Backend;

import com.DBMS.Backend.Metrics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class Accuracy extends Metrics {
    private ResultSet resultSet;
    private String col; // specify which column to be checked
    private String path;
    private int refNum;

    private float correctNum; // how many data rows is correct?

    public Accuracy(String tableName, String col, String refPath, String refNum, Statement statement) {
        super(tableName, statement);
        this.col = col;
        this.path = refPath;
        this.refNum = Integer.parseInt(refNum);

        String dataCommand = "SELECT " + tableName + ".[" + col + "] FROM " + tableName + ";";
//        System.out.println(dataCommand);
        try {
            this.resultSet = statement.executeQuery(dataCommand); // traversing the result set from the first statement
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public float calculate() {
        // load the csv data
        main.java.com.DBMS.Backend.AccuracyRef accuracyRef = new main.java.com.DBMS.Backend.AccuracyRef(this.path,
                refNum, true);
//        System.out.println(this.path);
        accuracyRef.csvRead();

        this.correctNum = 0; // initialize the correct number of value
//        System.out.println(totalNum);
        try {
            while (this.resultSet.next()) {
                String dataValue = resultSet.getString(col);
//                System.out.println(dataValue); // for debugging
                if (dataValue != null) {
                    if (accuracyRef.getRef().contains(dataValue.trim())) {
                        correctNum++;
                    }
                }
            } //todo: option, else: write down the incorrect one into txt
            return correctNum / super.getTotalNum();
        } catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
        }
        return 0;


    }
}
