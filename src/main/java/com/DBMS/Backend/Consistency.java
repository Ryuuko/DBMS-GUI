package com.DBMS.Backend;

import org.apache.commons.math3.stat.inference.BinomialTest;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.math3.stat.inference.AlternativeHypothesis.TWO_SIDED;


public class Consistency extends Metrics {

    private ResultSet groupedResult; // the count amount for each grouped data
    private String antecedent;
    private String consequent;
    private ConsistencyRef consistencyRef;


    public Consistency(String tableName, String antecedent, String consequent, String path, String ref1, String ref2,
                       Statement statement) {

        super(tableName, statement);

        this.antecedent = antecedent;
        this.consequent = consequent;

        String groupbyCommand = "SELECT " + antecedent + "," + consequent + "," + "Count(*) AS GroupCount\n" +
                "FROM " + tableName + "\n" +
                "WHERE " + antecedent + " IS NOT NULL And " + consequent + " IS NOT NULL\n" +
                // we check the null value in the completeness metrics instead
                "GROUP BY " + antecedent + "," + consequent;

        try {
            this.groupedResult = statement.executeQuery(groupbyCommand); // traversing the result set from the first statement
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.consistencyRef = new ConsistencyRef(path, Integer.parseInt(ref1), Integer.parseInt(ref2), true); //todo: let user choose reference, -1 if they choose the column

        consistencyRef.csvRead(); // load the data according the chosen columns
        consistencyRef.freqListBuild(); // build the frequency list


    }

    public double calculation() {
        double groupCount = 0;
        double scoreSum = 0;
        try {
            while (this.groupedResult.next()) {
                // put down the examined data
                List<String> examinedElement = new ArrayList<>(); // mutable object need to create new object every time
                int examinedFreq = groupedResult.getInt("GroupCount");
                examinedElement.add(groupedResult.getString(antecedent).trim()); // todo: You've assumed that both are string....how to deal with the numeric eg.int
                examinedElement.add(groupedResult.getString(consequent).trim());

//                System.out.println("We're going to examine this group:");
//                System.out.println(examinedElement);


//                System.out.println("List of our reference group:");
//                System.out.println(consistencyRef.freqListGetter());

                // load the reference's count
                float refCount = consistencyRef.freqListGetter().get(examinedElement);


                // run the following code if there's an appropriate combination exist in the reference dataset

                if (refCount != 0) {
                    double refFreq = (double) refCount / consistencyRef.totalGetter();
//                    System.out.println("examined Element:" + examinedElement); // for debugging
//                    System.out.println("examined frequency:" + examinedFreq);
//                    System.out.println("refFreq:" + refFreq);
//                    System.out.println("sample size:" + super.getTotalNum());

                    // load the two-side hypothesis algorithm
                    BinomialTest binomialTest = new BinomialTest();
                    double score = binomialTest.binomialTest((int) super.getTotalNum(),
                            examinedFreq, refFreq, TWO_SIDED);
                    System.out.println(score);
                    scoreSum += score;
                    groupCount++;
                }
            } //todo: option, else: write down the incorrect one into txt
//            return correctNum / this.totalNum;
        } catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
        }
//        System.out.println("The amount of groups: " + groupCount);
//        System.out.println("sum of score: " + scoreSum);

        return scoreSum / groupCount;
    }
}

