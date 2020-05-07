package com.DBMS.Backend.DataGetter;

import com.DBMS.Backend.ObjectClass.LoginParameter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionGetter {

    private String dbUrl;
    private Connection connection;

    public ConnectionGetter(LoginParameter input) {

        this.dbUrl = "jdbc:sqlserver://" + input.getHostname() +
                ";databaseName=" + input.getDatabaseName() +
                ";user=" + input.getUserName() + ";password=" + input.getPassword();
//        System.out.println(dbUrl); // check if you input correctly
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
