package com.DBMS.Backend;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Currency {
    private String tableName;
    private Statement statement;
    private int totalNum;
    private double[] ageUpdate;
    private double totalFreq;
    private Double avgCurrency; // use wrapper class in order to check nullness in the calculationTableLevel

    public Currency(String tableName, Statement statement) {
        this.tableName = tableName;
        this.statement = statement;
        String countCommand = "SELECT COUNT(*) AS Total\n" +
                "FROM " + tableName + ";";
        try {
            ResultSet resultSetTotal = statement.executeQuery(countCommand);
            if (resultSetTotal.next()) {
                this.totalNum = resultSetTotal.getInt("Total"); // save the count number in the member variable
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.print("The total number of record: " + totalNum + "\n");

        this.ageUpdate = new double[totalNum];
        this.totalFreq = 0; // initialize the total frequency from 0
    }

    /*use inner class to differentiate the row level and table level calculation*/

    public abstract class Level {

        public void calculation() {
            double avgFreq = avgFreq();
            System.out.println("The average frequency of the table is:");
            System.out.println(avgFreq);
            System.out.println("\n");

            for (int i = 0; i < ageUpdate.length; i++) {
                double currencyMetric = currecnyFormula(avgFreq, ageUpdate[i]);
                resultBuild(i, currencyMetric);
            }
        }

        private double currecnyFormula(double avgFreq, double ageUpdated) {
            return 1 / ((avgFreq * ageUpdated) + 1);
        }

        public abstract void resultBuild(int i, double currencyMetric);
    }

    public class RowLevel extends Level {
        @Override
        public void resultBuild(int i, double currencyMetric) {
            System.out.println("for row " + i + ":");
            System.out.println(currencyMetric);
            System.out.println("\n");
        }
    }

    public class TableLevel extends Level {
        double sum = 0.d;

        @Override
        public void resultBuild(int i, double currencyMetric) {
            // i unused here
            sum += currencyMetric;
        }

        public void show() {
            System.out.println("The average currency is: ");
            System.out.println(sum / totalNum); //todo: prevent the user call the result() firsrt
        }
    }

    public class rowTableLevel extends TableLevel {
        @Override
        public void resultBuild(int i, double currencyMetric) {
            System.out.println("for row " + (i + 1) + ":"); // i+1 make it starting from 1 and more user-friendly
            System.out.println(currencyMetric);
            sum += currencyMetric;
            System.out.println("\n");
        }
    }

    public double avgFreq() {
        // todo: avoid using the member variable all the time, get the return value from the dataLoader, eg. totalfreq
        // todo: avgfreq may be inistanlised as member variable?
        dataLoader();
//        System.out.print(totalFreq + "\n");
//        System.out.println(Arrays.toString(this.ageUpdate) + "\n");
        return totalFreq / (double) totalNum;
    }

    private void dataLoader() {
        // reform the transaction log's data and load the update frequency and the age of updated value

        for (int i = 0; i < totalNum; i++) { // todo: while (this.resultSet.next()) is enough?

            try {
//            System.out.println(command);

                String dataCommand = keyRowCommand(i) + logRebuild() + dataCollection();
                ResultSet resultSet = this.statement.executeQuery(dataCommand);

                while (resultSet.next()) {
                    /* for debugging */
//                    System.out.print("for the row " + (i + 1) + ":\n"); // i+1 make it starting from 1 and more user-friendly
//                    System.out.printf("%-30.30s  %-30.30s%n", "the update frequency", "the age of updated value");
//                    System.out.printf("%-30.30s  %-30.30s%n", resultSet.getFloat("updateFreq"),
//                            resultSet.getDouble("ageUpdate"));
                    this.ageUpdate[i] = resultSet.getDouble("ageUpdate");
                    this.totalFreq += resultSet.getDouble("updateFreq");
                }
            } catch (SQLException e) {
                System.out.println("Connection failure.");
                e.printStackTrace();
            }
        }
    }

    /*The following three functions are SQL-queries */

    private String keyRowCommand(int row) {

        // find the key for a row
        return
                "declare @keyLock nvarchar(20)\n" +
                        "\n" +
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
                        " WHERE AllocUnitName='dbo.Location') AS Subset\n" +
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

    private String dataCollection() {
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
                        "SELECT @updateFreq AS updateFreq, @ageUpdate AS ageUpdate;";
    }

    public double totalNumGetter() {
        return totalNum;
    }

}

