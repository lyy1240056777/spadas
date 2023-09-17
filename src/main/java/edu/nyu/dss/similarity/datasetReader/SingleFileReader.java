package edu.nyu.dss.similarity.datasetReader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.util.stream.Stream;

@Component
public class SingleFileReader {


    @Value("${spadas.file.baseUri}")
    private String basePath;


    // FIXME: the data lake ID is never assigned value. please refer the code in Framework to find out the initialized value of datalakeID.
    public static int datalakeID;// the lake id

    /*
     * this reads a single dataset after using the datalake index, instead of storing all the datasets in the main memory
     */
    public double[][] readSingleFile(String datasetid) throws FileNotFoundException, IOException {

        File file = new File(basePath + "/" + datasetid);
        long lineNumber = 0;
        try (Stream<String> lines = Files.lines(file.toPath())) {
            lineNumber = lines.count();
        }
        double[][] a = null;
        int i = 0;
        if (datalakeID == 4)
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {// reading argo
                String strLine;
                a = new double[(int) lineNumber - 1][];
                while ((strLine = br.readLine()) != null) {
                    String[] splitString = strLine.split(",");
                    String aString = splitString[6];
                    if (aString.matches("-?\\d+(\\.\\d+)?")) {
                        a[i] = new double[3];
                        a[i][0] = Double.parseDouble(splitString[6]);
                        a[i][1] = Double.parseDouble(splitString[7]);
                        a[i][2] = Double.parseDouble(splitString[1].replace("-", ""));
                        i++;
                    }
                }
            }
        else if (datalakeID == 3)
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String strLine;
                a = new double[(int) lineNumber][];
                while ((strLine = br.readLine()) != null) {
                    String[] splitString = strLine.split(" ");
                    a[i] = new double[3];
                    a[i][0] = Double.parseDouble(splitString[0]);
                    a[i][1] = Double.parseDouble(splitString[1]);
                    a[i][2] = Double.parseDouble(splitString[2]);
                    i++;
                }
            }
        else if (datalakeID == 5)
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String strLine;
                int[] mappingRule = new int[]{7, 8, 9, 10, 0, 1, 2, 3, 4, 5, 6};
                a = new double[(int) lineNumber][];
                while ((strLine = br.readLine()) != null) {
                    String[] splitString = strLine.split(",");
                    a[i] = new double[11];
                    for (int j = 0; j < 11; j++) {
                        a[i][j] = Double.parseDouble(splitString[mappingRule[i]]);
                    }
                    i++;
                }
            }
        return a;
    }
}
