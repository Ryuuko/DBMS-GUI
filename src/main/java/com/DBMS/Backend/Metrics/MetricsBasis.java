package com.DBMS.Backend.Metrics;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/*A Common class for fours Metrics classes, which need the name of table and data connection as in the parameter
 * input. Pre-process also the total number of rows, which are required in all Metrics class as well */


public class MetricsBasis {
    private float totalNum;
    private Statement statement;

    public MetricsBasis(String tableName, Connection connection) {

        try {
            this.statement = connection.createStatement();
            String countCommand = "SELECT COUNT(*) AS Total " + "FROM " + tableName + ";";
            ResultSet resultSetTotal = statement.executeQuery(countCommand);
            if (resultSetTotal.next()) {
                this.totalNum = resultSetTotal.getFloat("Total"); // save the count number in the member variable
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public float getTotalNum() {
        return totalNum;
    }

    public Statement getStatement() {
        return statement;
    }
}
