package edu.nyu.dss.similarity.datasetReader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;



@Deprecated
@Component
public class XianReader {

    @Value("${spadas.dimension}")
    private int dimension;

//    public Map<Integer, double[][]> readContentXian(File file, int fileNo, String filename) throws IOException {
////		long lineNumber = 0;
////		System.out.println(file.length());
//        int i = 0;
//        List<double[]> list = new ArrayList<>();
//        double[][] xxx;
//        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
//            String strLine;
////			LineNumberReader lnr = new LineNumberReader(new FileReader(file));
////			lineNumber = lnr.getLineNumber() + 1;
////			a = new double[(int) lineNumber-1][];
//            while ((strLine = br.readLine()) != null) {
//                String[] splitString = strLine.split(",");
//                String aString = splitString[0];
//                if (!aString.equals("uid")) {// only has float
////					a[i] = new double[dimension];
//                    double[] b = new double[dimension];
////					System.out.println(splitString[31]);
//                    if (!splitString[31].isEmpty() && !splitString[32].isEmpty()) {
//                        b[0] = Double.parseDouble(splitString[32]);
//                        b[1] = Double.parseDouble(splitString[31]);
//                        list.add(b);
//                        i++;
//                    }
//                }
//            }
//            xxx = list.toArray(new double[i][]);
//        }
//        if (i > 0) {
//            if (countHistogram.containsKey(i))
//                countHistogram.put(i, countHistogram.get(i) + 1);
//            else
//                countHistogram.put(i, 1);
//            if (storeAllDatasetMemory)
//                dataMapPorto.put(fileNo, xxx);
//            if (storeIndexMemory) {
////				createDatasetIndex(fileNo, xxx,1);
////				debug in the future
////				createDatasetIndex(fileNo, xxx, 1, cityNode);
//            }
//            if (!zcurveExist)
//                storeZcurve(xxx, fileNo);
//        }
//        return dataMapPorto;
//    }
}
