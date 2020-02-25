package com.DBMS.Backend;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class Metrics {
    private float totalNum; // how many rows in total?

    public Metrics(String tableName, Statement statement) {
        String countCommand = "SELECT COUNT(*) AS Total " + "FROM " + tableName + ";";
        System.out.println(countCommand);
        try {
            ResultSet resultSetTotal = statement.executeQuery(countCommand);
            if (resultSetTotal.next()) {
                this.totalNum = resultSetTotal.getFloat("Total"); // save the count number in the member variable
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public float getTotalNum() {
//        System.out.println(totalNum);
        return totalNum;
    }
}
