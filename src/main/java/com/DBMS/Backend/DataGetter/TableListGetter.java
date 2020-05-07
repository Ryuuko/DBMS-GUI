package com.DBMS.Backend.DataGetter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TableListGetter {

    List<String> tableList;

    public TableListGetter(Connection connection) {
        this.tableList = new ArrayList<>();
        String command = "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE='BASE TABLE';";
//        System.out.println(command); // for debugging
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(command);
            while (resultSet.next()) {
                this.tableList.add(resultSet.getString("TABLE_NAME")); // save all the table names in the member variable
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getTableList() {
        return tableList;
    }
}
