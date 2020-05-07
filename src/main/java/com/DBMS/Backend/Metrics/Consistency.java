package com.DBMS.Backend.Metrics;

import com.DBMS.Backend.ObjectClass.MetricsParameter;
import com.DBMS.Backend.ObjectClass.ReferenceParameter;
import com.DBMS.Backend.ObjectClass.RuleGroup;
import com.DBMS.Backend.Reference.ConsistencyRef;
import org.apache.commons.math3.stat.inference.BinomialTest;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.apache.commons.math3.stat.inference.AlternativeHypothesis.TWO_SIDED;


public class Consistency extends MetricsBasis {

    private ResultSet groupedResult; // the count amount for each grouped data
    private String antecedent;
    private String consequent;
    private ConsistencyRef consistencyRef;


    public Consistency(MetricsParameter input) {

        super(input.getTableName(), input.getConnection());

        this.antecedent = input.getColumns1();
        this.consequent = input.getColumns2();

        String groupByCommand = "SELECT " + antecedent + "," + consequent + "," + "Count(*) AS GroupCount\n" +
                "FROM " + input.getTableName() + "\n" +
                "WHERE " + antecedent + " IS NOT NULL And " + consequent + " IS NOT NULL\n" +
                // we check the null value in the completeness metrics instead
                "GROUP BY " + antecedent + "," + consequent;

        try {
            // traversing the result set from the first statement
            this.groupedResult = getStatement().executeQuery(groupByCommand);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ReferenceParameter referenceParameter = new ReferenceParameter(input.getPath(), input.isSkip());
        referenceParameter.setColumn1(Integer.parseInt(input.getRef1()));
        referenceParameter.setColumn2(Integer.parseInt(input.getRef2()));
        this.consistencyRef = new ConsistencyRef(referenceParameter);

        consistencyRef.readCsv(); // load the data according the chosen columns
        consistencyRef.freqListBuild(); // build the frequency list

    }

    public double calculate() {
        double groupCount = 0;
        double scoreSum = 0;

        try {
            while (this.groupedResult.next()) {
                // put down the examined data
                RuleGroup examinedGroup = new RuleGroup(groupedResult.getString(antecedent).trim(),
                        groupedResult.getString(consequent).trim());
                int examinedFreq = groupedResult.getInt("GroupCount");

                // load the reference's count
                if (consistencyRef.freqListGetter().containsKey(examinedGroup)) {
                    float refCount = consistencyRef.freqListGetter().get(examinedGroup);

                    // run the following code if there's an appropriate combination exist in the reference dataset
                    double refFreq = (double) refCount / consistencyRef.totalGetter();

                    // load the two-side hypothesis algorithm
                    BinomialTest binomialTest = new BinomialTest();
                    double score = binomialTest.binomialTest((int) super.getTotalNum(),
                            examinedFreq, refFreq, TWO_SIDED);

                    scoreSum += score;
                    groupCount++;
                }
            }
            groupedResult.close();
            getStatement().close();
        } catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
        }

        // if we found any group, return the division, else return 0
        return groupCount != 0 ? scoreSum / groupCount : 0;
    }
}

