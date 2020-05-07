package com.DBMS.Backend.Reference;

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

    public void readCsv() {

        String csvFile = this.csvFile;

        try {
            CSVReader reader = new CSVReader(new FileReader(csvFile));
            readDataIteration(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readDataIteration(CSVReader reader) {
        try {
            String[] line;
            if (this.skipHeader) {
                consumeHeader(reader);
            }

            while ((line = reader.readNext()) != null) {
                refBuilding(line);
            }

        } catch (CsvValidationException | IOException e) {
            e.printStackTrace();
        }
    }

    private void consumeHeader(CSVReader reader) {
        try {
            reader.readNext();
        } // consume the header before move on the reading loop
        catch (CsvValidationException | IOException e) {
            e.printStackTrace();
        }
    }

    public abstract void refBuilding(String[] line);

}
