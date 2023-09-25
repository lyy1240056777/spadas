package edu.nyu.dss.similarity.datasetReader;

import org.springframework.stereotype.Component;

@Deprecated
@Component
public class ShapeNetReader {

//    static Map<Integer, double[][]> readShapeNet(File folder, Map<Integer, double[][]> datasetMap, int limit) throws IOException {
//        File[] fileNames = folder.listFiles();
//        fileNo = 1;
//        datasetMap = null;
//        if (storeAllDatasetMemory)
//            datasetMap = new HashMap<>();
//        datasetIdMapping = new HashMap<>();
//        for (File file : fileNames) {
//            if (file.isDirectory()) {
//                //read the files inside
//                readShapeNet(file, datasetMap, limit);
//            } else {
//                if (!file.getName().contains("name")) {
//                    datasetIdMapping.put(fileNo, file.getName());
//                    long lineNumber = 0;
//                    try (Stream<String> lines = Files.lines(file.toPath())) {
//                        lineNumber = lines.count();
//                    }
//                    double[][] a = new double[(int) lineNumber][];
//                    int i = 0;
//                    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
//                        String strLine;
//                        while ((strLine = br.readLine()) != null) {
//                            // System.out.println(strLine);
//                            String[] splitString = strLine.split(" ");
//                            a[i] = new double[3];
//                            a[i][0] = Double.parseDouble(splitString[0]);
//                            a[i][1] = Double.parseDouble(splitString[1]);
//                            a[i][2] = Double.parseDouble(splitString[2]);
//                            i++;
//                        }
//                    }
//                    //	System.out.println(i+","+lineNumber);
//                    if (countHistogram.containsKey(a.length))// count the histograms
//                        countHistogram.put(a.length, countHistogram.get(a.length) + 1);
//                    else
//                        countHistogram.put(a.length, 1);
//                    if (storeAllDatasetMemory == true) {
//                        datasetMap.put(fileNo, a);
//                    }
//                    if (storeIndexMemory) {
//                        createDatasetIndex(fileNo, a);
//                    }
//                    if (!zcurveExist)
//                        storeZcurve(a, fileNo);
//                    //	Path sourceDirectory = file.toPath();// Paths.get("/Users/personal/tutorials/source");
//                    //  Path targetDirectory = Paths.get("/Users/sw160/Desktop/spadas-dataset/Shapenet-allInOne/"+fileNo+".txt");
//                    //  write("/Users/sw160/Desktop/spadas-dataset/Shapenet-allInOne/namemapping.txt", fileNo+","+file.getName()+"\n");
//                    //  Files.copy(sourceDirectory, targetDirectory); // write into the same folder
//                    fileNo++;
//                    if (fileNo > limit)
//                        break;
//                }
//            }
//        }
//        if (fileNo < limit)
//            limit = fileNo;
//        return datasetMap;
//    }
}
