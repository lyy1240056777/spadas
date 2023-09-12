package main.java.web.Utils;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Component
public class FileU {
    public static Pair<String[], String[][]> readPreviewDataset(File file, int max, double[][] data) throws IOException {
//        File file = findFiles(Framework.aString, filename);
//        File file = new File(Framework.aString + "/" + filename);
//        if (file.exists()) {
//            System.out.println();
//        }
        long lineNumber = 0;
//        try (Stream<String> lines = Files.lines(file.toPath())) {
//            lineNumber = lines.count();
//        }
        FileReader fr = new FileReader(file);
        LineNumberReader lnr = new LineNumberReader(fr);
        lnr.skip(Long.MAX_VALUE);
        lineNumber = lnr.getLineNumber() - 1;
        lnr.close();
        lineNumber = Math.max(lineNumber, max);
        lineNumber = Math.min(data.length, lineNumber);
        String[] header = null;
//        String[][] bodies = new String[(int)lineNumber][];
        List<String[]> bodiesList = new ArrayList<>();
        // display ten rows
        try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath()), "gbk"))) {
            String strLine;
            int i = 0;
//            int j = 0;
            String headerStr[] = br.readLine().split(",");
            if (headerStr.length == 2) {
                header = headerStr;
                while (i < lineNumber) {
                    String[] splitString = new String[2];
                    splitString[0] = "" + data[i][1];
                    splitString[1] = "" + data[i][0];
                    bodiesList.add(splitString);
                    i++;
                }
            } else {
                header = new String[9];
                System.arraycopy(headerStr, 1, header, 0, 7);
                System.arraycopy(headerStr, 11, header, 7, 2);
                while (((strLine = br.readLine()) != null) && i < lineNumber) {
                    String[] rawSplitString = strLine.split(",");
//                    if (rawSplitString.length < 3)
//                        System.out.println(file.toURI());
                    String[] splitString = new String[9];
                    System.arraycopy(rawSplitString, 1, splitString, 0, 7);
                    System.arraycopy(rawSplitString, 11, splitString, 7, 2);
                    double temp;
                    try {
                        temp = Double.parseDouble(rawSplitString[12]);
                    } catch (Exception e) {
                        System.out.println(e.toString());
                        continue;
                    }
                    if (temp != data[i][0]) {
                        continue;
                    }
//                    bodies[i-1] = splitString;
                    bodiesList.add(splitString);
//                        j++;
                    i++;
                }
            }
//            while (((strLine = br.readLine()) != null) && i <= lineNumber && j < data.length) {
//                String[] rawSplitString = strLine.split(",");
//                if (rawSplitString.length < 3)
//                    System.out.println(file.toURI());
//                String[] splitString = new String[9];
//                System.arraycopy(rawSplitString, 1, splitString, 0, 7);
//                System.arraycopy(rawSplitString, 11, splitString, 7, 2);
//                if (i == 0) {
//                    header = splitString;
//                } else {
//                    double temp;
//                    try {
//                        temp = Double.parseDouble(rawSplitString[12]);
//                    } catch (Exception e) {
//                        System.out.println(e.toString());
//                        continue;
//                    }
//                    if (temp != data[j][0]) {
//                        continue;
//                    }
////                    bodies[i-1] = splitString;
//                    bodiesList.add(splitString);
//                    j++;
//                }
//                i++;
//            }
        }
        System.out.println("bodies:" + bodiesList.size());
        String[][] bodies = new String[bodiesList.size()][];
        for (int i = 0; i < bodiesList.size(); i++) {
            bodies[i] = bodiesList.get(i);
        }
        return Pair.of(header, bodies);
    }
}
