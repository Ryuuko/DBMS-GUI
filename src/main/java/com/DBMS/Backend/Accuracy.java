package com.DBMS.Backend;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;


public class Accuracy extends Metrics {
    private String tableName;
    private String col; // specify which column to be checked
    private String path;
    private int refNum;
    private Statement statement;


    public Accuracy(String tableName, String col, String refPath, String refNum, Statement statement) {

        super(tableName, statement);
        this.col = col;
        this.tableName = tableName;
        this.path = refPath;
        this.refNum = Integer.parseInt(refNum);
        this.statement = statement;
    }

    abstract class CalculationMethod {
        float totalScore = getTotalNum();
        float score = 0;

        public float calculate() {
            main.java.com.DBMS.Backend.AccuracyRef accuracyRef =
                    new main.java.com.DBMS.Backend.AccuracyRef(path,
                            refNum, true);
//        System.out.println(this.path);
            accuracyRef.csvRead();
            /*Number initialization*/

            try {
                String dataCommand = "SELECT " + col + " FROM " + tableName + ";";
                //        System.out.println(dataCommand);
                ResultSet resultSet = statement.executeQuery(dataCommand); // traversing the result set from the first statement

                while (resultSet.next()) {
//                System.out.println(dataValue); // for debugging
                    if (resultSet.getString(col) != null) {
                        String dataValue = resultSet.getString(col).trim();
//                        System.out.println("The examined data value is:");
//                        System.out.println(dataValue);

                        if (accuracyRef.getRef().contains(dataValue)) {
//                            System.out.println("Correct one get score: 1");
                            score++; // full score for correct one
                        } else {
                            calculatePreciseScore(dataValue, accuracyRef.getRef());
                        }
                    } else {
                        reduceTotalNum();
                    }
//                    System.out.println("Score for this round is: ");
//                    System.out.println(this.score);
                }
            } catch (SQLException e) {
                System.out.println("Connection failure.");
                e.printStackTrace();
            }
//            System.out.println("total score is: ");
//            System.out.println(this.totalScore);
//            System.out.println("-------------------------------------");

            return this.totalScore != 0 ? score / this.totalScore : 0;
        }

        public abstract void calculatePreciseScore(String examinedValue, HashSet<String> refCandidates);

        public abstract void reduceTotalNum();

    }

    public class Traditional extends CalculationMethod {

        /*In traditional, we actually don't run anything in the method*/
        @Override
        public void calculatePreciseScore(String examinedValue, HashSet<String> refCandidates) {

        }

        @Override
        public void reduceTotalNum() {

        }
    }

    public class Levenshtein extends CalculationMethod {

        @Override
        public void calculatePreciseScore(String examinedValue, HashSet<String> refCandidates) {
            String refString = "";
            float maxDifference = 100; // set an impossible edit difference to be beat
            LevenshteinDistance levenshteinDistance = new LevenshteinDistance();

            for (String refCandidate : refCandidates) {

                int tempScore = levenshteinDistance.apply(examinedValue, refCandidate);
//                                System.out.println("the edit distance between" + dataValue +
//                                        refCandidate + "is: ");
//                                System.out.println(tempScore);
                if (tempScore < maxDifference) { //todo:should we set a super large maxScore?
                    // if maxScore happen to be very small and tempScore very large,
                    // then the string will still have full score
                    refString = refCandidate; // the reference String will be the temporary champion
                    maxDifference = tempScore;
                }
            }
//            System.out.println("The most similar reference String is " + refString);
//
//            System.out.println("The edit distance between them is: " +
//                    levenshteinDistance.apply(examinedValue, refString));

            float longerStringLen = (examinedValue.length() - refString.length()) > 0 ?
                    examinedValue.length() : refString.length();

            super.score += (1 - maxDifference / longerStringLen);

        }

        @Override
        public void reduceTotalNum() {
            super.totalScore = super.totalScore - 1; // we don't view the null value as the accurate or not}
        }
    }

}
