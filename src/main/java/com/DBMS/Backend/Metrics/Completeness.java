package com.DBMS.Backend.Metrics;

import com.DBMS.Backend.ObjectClass.MetricsParameter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Completeness extends MetricsBasis {
    private String tableName;
    private float totalNum;
    private List<String> columns;

    public Completeness(MetricsParameter input) {
        super(input.getTableName(), input.getConnection());
        this.tableName = input.getTableName();
        this.totalNum = this.getTotalNum();
        searchAllColumns();
    }

    private abstract class Level {
        private float nullAmount = 0;

        public void runQuery(String command) {
            try {
                ResultSet resultSet = getStatement().executeQuery(command);
                // only one row is returned, no need to use while(resultSet.next())
                if (resultSet.next()) {
                    nullAmount += resultSet.getInt(1); // 1 for the first column
                }
                resultSet.close();
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
                    "FROM " + tableName + " WHERE " + column + " IS NULL;";
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
            // todo: it may be meaningful to skip the primary key since it's usually not null
            // table level is calculated as the aggregation of all attribute levels
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

    public void searchAllColumns() {
        this.columns = new ArrayList<>();
        String command = "select COLUMN_NAME from information_schema.columns where table_name = '" +
                tableName + "' ;";
        try {
            ResultSet resultSet = getStatement().executeQuery(command);
            while (resultSet.next()) {
                this.columns.add(resultSet.getString(1)); // save the count name in the member variable

            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        this.columns.forEach(System.out::println); // for debugging
    }
}
