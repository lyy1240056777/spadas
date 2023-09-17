package edu.nyu.dss.similarity.datasetReader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Deprecated
@Component
public class ArgoReader {

    @Value("${spadas.dimension}")
    private int dimension;

//    public Map<Integer, double[][]> readContentArgo(File file, int fileNo, String fileName, CityNode cityNode) {
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
//                if (aString.equals("TIMESTAMP")) {
//                    continue;
//                }
//                double[] b = new double[dimension];
//                b[0] = Double.parseDouble(splitString[6]);
//                b[1] = Double.parseDouble(splitString[7]);
//                if (b[1] < -85 || b[1] > -75) {
//                    continue;
//                }
//                list.add(b);
//                i++;
////					System.out.println(splitString[31]);
////                if (!splitString[12].equals("") && !splitString[11].equals("")) {
//////                    try {
//////                        b[0] = Double.parseDouble(splitString[12]);
//////                        b[1] = Double.parseDouble(splitString[11]);
//////                    } catch (Exception e) {
//////                        System.out.println(e);
//////                        System.out.println(file.getName());
//////                        System.out.println(splitString[12]);
//////                        System.out.println(splitString[11]);
//////                        continue;
//////                    }
//////                    if (b[0] < 10 || b[1] < 10) {
//////                        continue;
//////                    }
////                    list.add(b);
////                    i++;
////                }
//            }
//            xxx = list.toArray(new double[i][]);
////            System.out.println("File " + file.getName() + " has " + list.size() + " lines");
//            dataPoint.addAll(list);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
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
//                createDatasetIndex(fileNo, xxx, 1, cityNode);
//            }
//            if (!zcurveExist)
//                storeZcurve(xxx, fileNo);
//        }
//        return dataMapPorto;
//    }
}
