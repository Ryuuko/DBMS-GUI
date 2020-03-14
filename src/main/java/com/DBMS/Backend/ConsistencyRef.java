package com.DBMS.Backend;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConsistencyRef extends main.java.com.DBMS.Backend.CSVLoader {
    private int antecedent;
    private int consequent;
    private List<List> list;
    private Map<List<String>, Long> freqList;
    private long total;

    public ConsistencyRef(String url, int antecedent, int consequent, boolean skip) {
        super(url, skip);
        this.antecedent = antecedent - 1; // the column number start from 1 in GUI
        this.consequent = consequent - 1;
        this.list = new ArrayList<>();
    }

    /*override the method in csvReader's csvRead()*/
    @Override
    public void refBuilding(String[] line) {
        List<String> refElement = new ArrayList<>(); // mutable object need to create new object every time
        refElement.add(line[this.antecedent]); // todo: You've assumed that both are string....how to deal with the numeric eg.int
        refElement.add(line[this.consequent]);
        list.add(refElement);
        total++;
//      System.out.println(refElement);
    }

    public void freqListBuild() {
        this.freqList = this.list.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    public Map<List<String>, Long> freqListGetter() {
        return freqList;
    }

    public long totalGetter() {
        return total;
    }
}
