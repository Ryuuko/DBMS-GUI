package com.DBMS.Backend;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ColumnsGetter {
    private List<String> columns;

    public ColumnsGetter(Connection connection, String tableName) {
        this.columns = new ArrayList<>();
        String command = "select COLUMN_NAME from information_schema.columns where table_name = '" +
                tableName + "\' ;";
//        System.out.println(command); // for debugging
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(command);
            while (resultSet.next()) {
                this.columns.add(resultSet.getString(1)); // save the count name in the member variable
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getColumns() {
        return columns;
    }
}

