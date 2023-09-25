package edu.nyu.dss.similarity.datasetReader;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.stream.Stream;

@Deprecated
@Component
public class ChicagoReader {

    /*
     * read single dataset in a chicago folder
     */
    public static Map<Integer, double[][]> read(File file, int fileNo, String filename) throws IOException {
//        long lineNumber = 0;
//        try (Stream<String> lines = Files.lines(file.toPath())) {
//            lineNumber = lines.count();
//        }
//        double[][] a = new double[(int) lineNumber][];
//        int i = 0;
//        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
//            String strLine;
//            while ((strLine = br.readLine()) != null) {
//                // System.out.println(strLine);
//                String[] splitString = strLine.split(",");
//				/*	Boolean isnumberBoolean = true;
//				for(int j=0; j<11; j++)
//					if(splitString[j].isEmpty() || splitString[j].matches("-?\\d+(\\.\\d+)?")==false) {
//						isnumberBoolean = false;
//						break;
//					}
//				if (isnumberBoolean) {*/
//                //	write(filenameString, strLine+"\n");
//                //	System.out.println(strLine);
//                int[] indexMap = new int[]{7, 8, 9, 10, 0, 1, 2, 3, 4, 5, 6};
//                a[i] = new double[dimension];
//                for (int j = 0; j < indexMap.length; j++) {
//                    a[i][j] = Double.parseDouble(splitString[indexMap[j]]);
//                }
//                i++;
//            }
//        }
//        if (i > 0) {
//            if (countHistogram.containsKey(i))
//                countHistogram.put(i, countHistogram.get(i) + 1);
//            else
//                countHistogram.put(i, 1);
//            if (storeAllDatasetMemory)
//                dataMapPorto.put(fileNo, a);
//            if (storeIndexMemory) {
//                createDatasetIndex(fileNo, a);
//            }
//            if (!zcurveExist)
//                storeZcurve(a, fileNo);
//        }
//        return dataMapPorto;
        return null;
    }
}
