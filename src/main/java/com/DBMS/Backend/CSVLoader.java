package main.java.com.DBMS.Backend;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.IOException;

public abstract class CSVLoader {
    private String csvFile;
    private boolean skipHeader; // the user can choose to skip the header

    public CSVLoader(String url, boolean skip) {
        this.csvFile = url;
        this.skipHeader = skip;
    }

    public void csvRead() {
        CSVReader reader = null;
        String csvFile = this.csvFile;
        try {
            reader = new CSVReader(new FileReader(csvFile));

            try {
                String[] line;
                if (this.skipHeader) {
                    try {
                        String[] headerLine = reader.readNext();
                    } // consume the header before move on the reading loop
                    catch (CsvValidationException e) {
                        e.printStackTrace();
                    }
                }

                while ((line = reader.readNext()) != null) {
                    refBuilding(line);
                }
            } catch (CsvValidationException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("The references have been successfully loaded");
    }

    public abstract void refBuilding(String[] line);

}
