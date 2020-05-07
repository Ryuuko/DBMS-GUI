package com.DBMS.Backend.Metrics;

import com.DBMS.Backend.ObjectClass.MetricsParameter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class Currency extends MetricsBasis {
    private String tableName;
    private HashMap<Long, Double> pkMapAgeUpdate;
    private double totalUpdateFreq;
    private String primaryKey;


    public Currency(MetricsParameter input) {
        super(input.getTableName(), input.getConnection());
        this.tableName = input.getTableName();
        this.pkMapAgeUpdate = new HashMap<>();
    }

    private abstract class Level {
        double avgFreq; //todo:it might be considered to set as member variable, in order to run once only

        Level() {
            this.avgFreq = getAvgFreq();
            totalUpdateFreq = 0;
        }

        public double currencyFormula(double ageUpdated) {
            return 1 / ((avgFreq * ageUpdated) + 1);
        }

    }

    public double getAvgFreq() {

        loadTimeData();
        return totalUpdateFreq / (double) getTotalNum();
    }

    private void loadTimeData() {
        // reform the transaction log's data and load the update frequency and the age of updated value
        for (int i = 0; i < getTotalNum(); i++) {

            try {

                String sqlQuery = findPkOfRow(i) + findKeyOfRow(i) + logRebuild() + calculateFreqAge();
                ResultSet resultSet = getStatement().executeQuery(sqlQuery);

                while (resultSet.next()) {
                    this.pkMapAgeUpdate.put(resultSet.getLong("primaryKey"), resultSet.getDouble("ageUpdate"));
                    this.totalUpdateFreq += resultSet.getDouble("updateFreq");
                }
                resultSet.close();
//                Statement it can't be closed, since the two levels can be chosen together
            } catch (SQLException e) {
                System.out.println("Connection failure.");
                e.printStackTrace();
            }
        }
    }

    private String findPkOfRow(int row) {
        return
                "declare @primaryKey nvarchar(20)\n" +
                        "SET @primaryKey = (\n" +
                        "SELECT " + this.primaryKey +
                        " FROM " + this.tableName + "\n" +
                        "ORDER BY 1 \n" +
                        "OFFSET " + row + " ROWS\n" +  // move the cursor to next row
                        "FETCH NEXT 1 ROWS ONLY);\n";
    }


    private String findKeyOfRow(int row) {
        return
                "declare @keyLock nvarchar(20)\n" +
                        "SET @keyLock = (\n" +
                        "SELECT  %%lockres%%\n" +
                        "FROM " + this.tableName + "\n" +
                        "ORDER BY 1 \n" + // assume there's no hash collision and key lock is unique
                        "OFFSET " + row + "ROWS\n" +  // move the cursor to next row
                        "FETCH NEXT 1 ROWS ONLY);\n";
    }

    private String logRebuild() {
        // check the time log according to keyLock, save it as subquery
        return
                "DECLARE @timeLog TABLE(\n" + // create a table to save the subquery
                        "LockKey CHAR(130),\n" +
                        "Operation CHAR(20),\n" +
                        "Time_Record datetime2\n" +
                        ");\n" +
                        "\n" +
                        "INSERT INTO @timeLog \n" + // form the time log according the transaction commit time
                        "SELECT Subset.[Lock Information] , Subset.[Operation], Whole.[End Time]\n" +
                        "FROM\n" +
                        "(SELECT [Current LSN],\n" +
                        " [Transaction ID],\n" +
                        " [Operation],\n" +
                        "  [Page ID],\n" +
                        " [Slot ID],\n" +
                        "  [Transaction Name],\n" +
                        " [CONTEXT],\n" +
                        "  [Lock Information],\n" +
                        " [AllocUnitName]\n" +
                        " FROM sys.fn_dblog(NULL,NULL)\n" +
                        " WHERE AllocUnitName='dbo." + this.tableName + "') AS Subset\n" +
                        " JOIN \n" + // Self-Join the one which provide the transaction commit's timestamp
                        " (SELECT \n" +
                        " [Transaction ID],\n" +
                        " [Operation],\n" +
                        " [Transaction Name],\n" +
                        " [End Time]\n" +
                        " FROM sys.fn_dblog(NULL,NULL)\n" +
                        " WHERE Operation='LOP_COMMIT_XACT') AS Whole\n" +
                        " ON Subset.[Transaction ID] = Whole.[Transaction ID]\n" +
                        " WHERE (SELECT \n" +
                        "     CHARINDEX(@keyLock, Subset.[Lock Information])) > 0\n" +
                        "ORDER BY [End Time] ASC;\n";
    }

    private String calculateFreqAge() {
        // summarize two necessary data: update frequency and age for that row
        return
                // --how many time does the row get updated?
                "DECLARE @N FLOAT\n" +
                        "SET @N = (SELECT COUNT(*) FROM @timeLog)-1;\n" +
                        "\n" +
                        //         "--what's the age of the row?\n"
                        "DECLARE @age FLOAT\n" +
                        "SET @age=\n" +
                        // remember to use second as the datepart
                        // using millisecond datepart, we can only compute the datediff of 24 days!
                        "(SELECT datediff(second, timeLog.Time_Record, SYSDATETIME()) FROM \n" +
                        "(SELECT TOP 1 Time_Record FROM @timeLog) AS timeLog);\n" +
                        "\n" +
                        //        "--The update frequency per millisecond\n" +
                        "DECLARE @updateFreq Float\n" +
                        "SET @updateFreq = @N/@age\n" +
                        "\n" +
                        //       "--what's the age of most updated value\n" +
                        "DECLARE @ageUpdate FLOAT\n" +
                        "SET @ageUpdate=\n" +
                        "(SELECT datediff(second, timeLog.Time_Record, SYSDATETIME()) FROM \n" +
                        "(SELECT TOP 1 Time_Record FROM @timeLog ORDER BY Time_Record DESC --make sure to use descendent order\n" +
                        ") timeLog);\n" +
                        "\n" +
                        //       "--return one single table with all the necessary information\n" +
                        "SELECT @updateFreq AS updateFreq, @ageUpdate AS ageUpdate, @primaryKey As primaryKey;";
    }


    public class RowLevel extends Level {
        long pkValue;

        public RowLevel(long pkValue) {
            this.pkValue = pkValue;
        }

        public double calculate() {
            return currencyFormula(pkMapAgeUpdate.get(pkValue));
        }

    }

    public class TableLevel extends Level {


        public double calculate() {
            double sum = pkMapAgeUpdate.values().stream()
                    .reduce(0.d, (partialSum, value) -> partialSum + currencyFormula(value));
//            the parsame as :
//            double oldSum = 0.d;
//            for (double value : pkMapAgeUpdate.values()) {
//                oldSum += currencyFormula(value);
//            }

            return getTotalNum() != 0 ? sum / (double) getTotalNum() : 0;
        }
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }
}

