package com.DBMS.Backend.Reference;

import com.DBMS.Backend.ObjectClass.ReferenceParameter;
import com.DBMS.Backend.ObjectClass.RuleGroup;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConsistencyRef extends CSVLoader {
    private int antecedentNum;
    private int consequentNum;
    private ArrayList<RuleGroup> combinationList;
    private Map<RuleGroup, Long> freqMap;
    private long total;

    public ConsistencyRef(ReferenceParameter input) {
        super(input.getPath(), input.isSkip());
        this.antecedentNum = input.getColumn1() - 1; // the column number start from 1 in GUI
        this.consequentNum = input.getColumn2() - 1;
        this.combinationList = new ArrayList<>();
    }

    @Override
    public void refBuilding(String[] line) {

        RuleGroup newRule = new RuleGroup(line[this.antecedentNum], line[this.consequentNum]);
        combinationList.add(newRule);
        total++;
//      System.out.println(refElement);
    }

    // Form the freqMap to record each group with its frequency(count) in the reference dataset
    // just like groupby in SQL
    public void freqListBuild() {
        this.freqMap = this.combinationList.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    public Map<RuleGroup, Long> freqListGetter() {
        return freqMap;
    }

    public long totalGetter() {
        return total;
    }
}
