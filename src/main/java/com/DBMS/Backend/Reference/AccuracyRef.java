package com.DBMS.Backend.Reference;

import com.DBMS.Backend.ObjectClass.ReferenceParameter;

import java.util.HashSet;

public class AccuracyRef extends CSVLoader {
    private HashSet<String> refSet;
    private int col; // the user can choose which column should be the reference

    public AccuracyRef(ReferenceParameter input) {
        super(input.getPath(), input.isSkip());
        this.col = input.getColumn1() - 1; // an intuitive way to view the first column as 1 rather than 0
        this.refSet = new HashSet<>();
    }

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
