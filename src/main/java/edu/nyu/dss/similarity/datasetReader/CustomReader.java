package edu.nyu.dss.similarity.datasetReader;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Stream;

@Deprecated
@Component
public class CustomReader {

    public static void read(File file, int fileNo) throws IOException {
        long lineNumber;
        try (Stream<String> lines = Files.lines(file.toPath())) {
            lineNumber = lines.count();
        }
        double[][] a = new double[(int) lineNumber - 1][];// a little different
        int i = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String strLine;
            while ((strLine = br.readLine()) != null) {
                String[] splitString = strLine.split(",");
                if (splitString.length < 3)
                    System.out.println(file.toURI());
                String aString = splitString[3];
                if (aString.matches("-?\\d+(\\.\\d+)?")) {// only has float
                    a[i] = new double[3];
                    a[i][0] = Double.parseDouble(splitString[6]);
                    a[i][1] = Double.parseDouble(splitString[7]);
                    a[i][2] = Double.parseDouble(splitString[1].replace("-", ""));
                    i++;
                }
            }
        }
//        if (countHistogram.containsKey(i))
//            countHistogram.put(i, countHistogram.get(i) + 1);
//        else
//            countHistogram.put(i, 1);
//        if (storeAllDatasetMemory)
//            dataMapPorto.put(fileNo, a);
//        if (storeIndexMemory) {
////			createDatasetIndex(fileNo, a, 0, dataLakeMapping.get(fileNo));
//        }
//        if (!zcurveExist)
//            storeZcurve(a, fileNo);
    }
}
