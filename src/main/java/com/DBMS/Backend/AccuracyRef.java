package main.java.com.DBMS.Backend;

import java.util.HashSet;

public class AccuracyRef extends main.java.com.DBMS.Backend.CSVLoader {
    private HashSet<String> refSet;
    private int col; // the user can choose which column should be the reference

    public AccuracyRef(String url, int num, boolean skip) {
        super(url, skip);
        this.col = num - 1; // an intuitive way to view the first column as 1 rather than 0
        this.refSet = new HashSet<>();
    }

    /*override the method in csvReader's csvRead()*/
    @Override
    public void refBuilding(String[] line) {
        String refElement = line[this.col];
//        System.out.println(refElement);
        this.refSet.add(refElement);
    }

    // via the getter to get the private member variable
    public HashSet<String> getRef() {
        return this.refSet;
    }
}
