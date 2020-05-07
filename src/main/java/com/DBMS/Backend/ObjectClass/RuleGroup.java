package com.DBMS.Backend.ObjectClass;

/*This class is used when we need to hash each group which is formed by an arrayList of two Strings in
in calculation of the Consistency metric
The structure is imitated the bigram from the answer of aioobe
https://stackoverflow.com/questions/9973596/arraylist-as-key-in-hashmap*/


public final class RuleGroup {
    private final String antecedent, consequent;

    public RuleGroup(String antecedent, String consequent) {
        this.antecedent = antecedent;
        this.consequent = consequent;
    }

    public String getAntecedent() {
        return antecedent;
    }

    public String getConsequent() {
        return consequent;
    }

    /*Then we need to override methods of hashCode and equal. Hashcode is help us hash the list
    in the hashing data structure like HashMap; equal is to force Java to admit two rules classes
    with the same attribute value are equal, even though they are differently hashed and hence
    differently stored in memory. More detail in thearticle of Hussein Terek in
    https://dzone.com/articles/working-with-hashcode-and-equals-in-java
     */


    @Override
    public int hashCode() {
        return antecedent.hashCode() ^ consequent.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof RuleGroup) && ((RuleGroup) obj).antecedent.equals(antecedent)
                && ((RuleGroup) obj).consequent.equals(consequent);
    }
}


