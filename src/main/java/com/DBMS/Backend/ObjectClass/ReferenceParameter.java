package com.DBMS.Backend.ObjectClass;

/*Pack all the possible parameter inputs into the class. This class will be used by the accuracy
and consistency's references*/

public final class ReferenceParameter {
    private String path;
    private boolean skip;
    private int column1;
    private int column2;


    public ReferenceParameter(String pathInput, boolean skipInput) {
        this.path = pathInput;
        this.skip = skipInput;

    }

    public String getPath() {
        return path;
    }

    public boolean isSkip() {
        return skip;
    }

    public int getColumn1() {
        return column1;
    }

    public void setColumn1(int column1) {
        this.column1 = column1;
    }

    public int getColumn2() {
        return column2;
    }

    public void setColumn2(int column2) {
        this.column2 = column2;
    }
}
