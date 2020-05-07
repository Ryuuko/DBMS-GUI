package com.DBMS.Backend.ObjectClass;

import java.sql.Connection;

public final class MetricsParameter {
    private String tableName;
    private Connection connection;
    private String columns1;
    private String columns2;
    private String path;
    private String ref1;
    private String ref2;
    private boolean skip;

    // use setter approach rather than constructor, since different metrics have different parameter required to input

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public String getColumns1() {
        return columns1;
    }

    public void setColumns1(String columns1) {
        this.columns1 = columns1;
    }

    public String getColumns2() {
        return columns2;
    }

    public void setColumns2(String columns2) {
        this.columns2 = columns2;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRef1() {
        return ref1;
    }

    public void setRef1(String ref1) {
        this.ref1 = ref1;
    }

    public String getRef2() {
        return ref2;
    }

    public void setRef2(String ref2) {
        this.ref2 = ref2;
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }
}
