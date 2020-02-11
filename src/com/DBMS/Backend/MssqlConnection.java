package com.DBMS.Backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MssqlConnection {

    private String dbUrl;
    private Connection connection;

    public MssqlConnection(String hostname, String database, String username, String password) {

        this.dbUrl = "jdbc:sqlserver://" + hostname + ";databaseName=" + database +
                ";user=" + username + ";password=" + password;
        System.out.println(dbUrl);
    }

    public boolean startConnection() {
        try {
            this.connection = DriverManager.getConnection(dbUrl);
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Connection connectionGetter() {
        return this.connection;
    }
}
