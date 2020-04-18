package com.DBMS.Backend;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Completeness extends Metrics {
    private String tableName;
    private Statement statement;
    private float totalNum;
    private List<String> columns;

    public Completeness(String tableName, Statement statement) {
        super(tableName, statement);
        this.tableName = tableName;
        this.statement = statement;
        this.totalNum = this.getTotalNum();
//        System.out.print("The total number of record: " + totalNum + "\n");
        columnsInput();
    }

    private abstract class Level {
        private float nullAmount = 0;

        public void runQuery(String command) {
            try {
                ResultSet resultSet = statement.executeQuery(command);
                // only one row is returned, no need to use while(resultSet.next())
                if (resultSet.next()) {
                    nullAmount += resultSet.getInt(1); // 1 for the first column
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public float getNullAmount() {
            return nullAmount;
        }

        public abstract float calculate();

    }

    public class AttributeLevel extends Level {

        public AttributeLevel(String column) {
            String command = "SELECT Count(*) \n" +
                    "FROM " + tableName + " WHERE " + column + " IS NULL;"; //todo: add double quote for safe?
//            System.out.println(command);
            super.runQuery(command);
        }

        @Override
        public float calculate() {
            return 1 - super.getNullAmount() / totalNum;
        }
    }


    public class RowLevel extends Level {
        public RowLevel(String pkCol, String pk) {

            if (!columns.contains(pkCol)) {
                System.out.println("Your Primary Key is not in the columns");
            } else {
                for (String col : columns) {
                    // we check the null value in all the columns except the primary key, since it's assumed to be not null
                    // also we assume we have the primary key not necessarily to be the first column

                    if (!col.equals(pkCol)) {
                        String command = "SELECT Count(*) " +
                                "FROM " + tableName + " WHERE \"" + pkCol + "\"=" + pk + " AND \"" +
                                col + "\" IS NULL;";
//                        System.out.println(command); // for debugging
                        super.runQuery(command);
                    }
                }
            }
        }

        @Override
        public float calculate() {
            return 1 - (super.getNullAmount() / (float) (columns.size() - 1));
        }
    }


    public class TableLevel extends Level {
        public TableLevel() {
            // todo: for the first time, we don't ignore the primary key case
            for (String col : columns) {
                String command = "SELECT Count(*) \n" +
                        "FROM " + tableName + " WHERE \"" + col + "\" IS NULL;";
                runQuery(command);
            }
        }

        @Override
        public float calculate() {
            return 1 - getNullAmount() / (totalNum * columns.size());
        }

    }

    // initialize the string array columns
    public void columnsInput() {
        this.columns = new ArrayList<>();
        String command = "select COLUMN_NAME from information_schema.columns where table_name = '" +
                tableName + "\' ;";
//        System.out.println(command); // for debugging
        try {
            ResultSet resultSet = statement.executeQuery(command);
            while (resultSet.next()) {
                this.columns.add(resultSet.getString(1)); // save the count name in the member variable
                //todo: try using resultSet.getString(1)
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        this.columns.forEach(System.out::println); // for debugging
    }
}
