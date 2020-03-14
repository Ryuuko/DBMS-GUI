package com.DBMS.Backend;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class Currency extends Metrics {
    private String tableName;
    private Statement statement;
    private HashMap<Long, Double> pkMapAgeUpdate;
    private double totalFreq;
    private String primaryKey;

    public Currency(String tableName, Statement statement) {
        super(tableName, statement);
        this.tableName = tableName;
        this.statement = statement;
        this.pkMapAgeUpdate = new HashMap<>();
        this.totalFreq = 0; // initialize the total frequency from 0
    }

    private void loadTimeData() {
        // reform the transaction log's data and load the update frequency and the age of updated value

        for (int i = 0; i < getTotalNum(); i++) { // todo: while (this.resultSet.next()) is enough?

            try {
//            System.out.println(command);

                String sqlQuery = findPkOfRow(i) + findKeyOfRow(i) + logRebuild() + calculateFreqAge();
//                System.out.println(sqlQuery); // debugging: check if the SQL is correctly written
                ResultSet resultSet = this.statement.executeQuery(sqlQuery);

                while (resultSet.next()) {
                    /* for debugging */
//                    System.out.print("for the row " + (i + 1) + ":\n"); // i+1 make it starting from 1 and more user-friendly
//                    System.out.printf("%-30.30s  %-30.30s%n", "the update frequency", "the age of updated value");
//                    System.out.printf("%-30.30s  %-30.30s%n", resultSet.getFloat("updateFreq"),
//                            resultSet.getDouble("ageUpdate"));
//                    System.out.println("For the primary key value:" + resultSet.getLong("primaryKey"));
                    this.pkMapAgeUpdate.put(resultSet.getLong("primaryKey"), resultSet.getDouble("ageUpdate"));
                    this.totalFreq += resultSet.getDouble("updateFreq");
                }
            } catch (SQLException e) {
                System.out.println("Connection failure.");
                e.printStackTrace();
            }
        }
    }

    /*The following three functions are SQL-queries */
    private String findPkOfRow(int row) {
        if (this.primaryKey == null) {
            primaryKey = "1"; // if no primary key is set, just assume it's normally the first column
        }
        return
                "declare @primaryKey nvarchar(20)\n" +
                        "SET @primaryKey = (\n" +
                        "SELECT " + this.primaryKey +
                        " FROM " + this.tableName + "\n" +
                        "ORDER BY 1 \n" +
                        "OFFSET " + row + "ROWS\n" +  // move the cursor to next row
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
                        "(SELECT datediff(millisecond, timeLog.Time_Record, SYSDATETIME()) FROM \n" +
                        "(SELECT TOP 1 Time_Record FROM @timeLog) AS timeLog);\n" +
                        "\n" +
                        //        "--The update frequency per millisecond\n" +
                        "DECLARE @updateFreq Float\n" +
                        "SET @updateFreq = @N/@age\n" +
                        "\n" +
                        //       "--what's the age of most updated value\n" +
                        "DECLARE @ageUpdate FLOAT\n" +
                        "SET @ageUpdate=\n" +
                        "(SELECT datediff(millisecond, timeLog.Time_Record, SYSDATETIME()) FROM \n" +
                        "(SELECT TOP 1 Time_Record FROM @timeLog ORDER BY Time_Record DESC --make sure to use descendent order\n" +
                        ") timeLog);\n" +
                        "\n" +
                        //       "--return one single table with all the necessary information\n" +
                        "SELECT @updateFreq AS updateFreq, @ageUpdate AS ageUpdate, @primaryKey As primaryKey;";
    }

    /*use inner class to differentiate the row level and table level calculation*/
    private abstract class Level {
        double avgFreq = getAvgFreq();

        public double currencyFormula(double ageUpdated) {
            return 1 / ((avgFreq * ageUpdated) + 1);
        }

    }

    public double getAvgFreq() {
        // todo: avoid using the member variable all the time, get the return value from the dataLoader, eg. totalfreq
        // todo: avgfreq may be inistanlised as member variable?
        loadTimeData();
        System.out.print("the total update frequency is: ");
        System.out.print(totalFreq + "\n");
        return totalFreq / (double) getTotalNum();
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
        double sum = 0.d;

        public double calculate() {
            System.out.println("The average frequency of the table is:");
            System.out.println(avgFreq);
            System.out.println("\n");

            for (double value : pkMapAgeUpdate.values()) {
                sum += currencyFormula(value);
            }
            return sum;
        }
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }
}

