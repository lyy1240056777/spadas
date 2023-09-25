package edu.nyu.dss.similarity.datasetReader;

import edu.nyu.dss.similarity.CityNode;
import edu.nyu.dss.similarity.statistics.DatasetSizeCounter;
import edu.nyu.dss.similarity.statistics.PointCounter;
import edu.rmit.trajectory.clustering.kmeans.indexNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Deprecated
@Component
public class LinesReader {

    @Value("${spadas.dimension}")
    private int dimension;

    @Autowired
    private DatasetSizeCounter datasetSizeCounter;

    @Autowired
    private PointCounter pointCounter;

    public Map<Integer, double[][]> read(File file, int fileNo, CityNode cityNode, int datasetIDForOneDir, String fileName) throws IOException {
        int i = 0;
        List<double[]> list = new ArrayList<>();
        double[][] xxx;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String strLine;
//			LineNumberReader lnr = new LineNumberReader(new FileReader(file));
//			lineNumber = lnr.getLineNumber() + 1;
//			a = new double[(int) lineNumber-1][];
            while ((strLine = br.readLine()) != null) {
                String[] splitString = strLine.split(",");
                String aString = splitString[0];
                if (aString.equals("lng")) {
                    continue;
                }
                double[] b = new double[dimension];
//					System.out.println(splitString[31]);
                if (!splitString[1].isEmpty() && !splitString[0].isEmpty()) {
                    try {
//                        第一个值是纬度lat，第二个值是经度lng
                        b[0] = Double.parseDouble(splitString[1]);
                        b[1] = Double.parseDouble(splitString[0]);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        System.out.println(file.getName());
                        System.out.println(splitString[1]);
                        System.out.println(splitString[0]);
                        continue;
                    }
//                    if (b[0] < 10 || b[1] < 10) {
//                        continue;
//                    }
//                    过滤掉不符合中国经纬度范围的数据
//                    if (b[0] < 3 || b[0] > 54 || b[1] < 73 || b[1] > 136) {
//                        continue;
//                    }
//                    过滤掉不符合经纬度范围的数据
                    if (b[0] < -90 || b[0] > 90 || b[1] < -180 || b[1] > 180) {
                        continue;
                    }
                    list.add(b);
                    i++;
                }
            }
            xxx = list.toArray(new double[i][]);
//            System.out.println("File " + file.getName() + " has " + list.size() + " lines");
            pointCounter.put(list.size());
        } catch (IOException e) {
            throw e;
        }
        if (i > 0) {
            datasetSizeCounter.put(i);
//            if (storeAllDatasetMemory) {
//                dataMapPorto.put(fileNo, xxx);
//                dataMapForEachDir.put(datasetIDForOneDir, xxx);
//            }
//            if (storeIndexMemory) {
////				createDatasetIndex(fileNo, xxx,1);
//                indexNode node = createDatasetIndex(fileNo, xxx, 1, cityNode);
////                对数据进行基于网格的取样，减小数据量
//                samplingDataByGrid(xxx, fileNo, node);
//            }
//            datasetIdMapping.put(fileNo, fileName);
//            if (!zcurveExist) {
//                storeZcurve(xxx, fileNo, 5, 5, 30, 100);
////                EffectivenessStudy.SerializedZcurve(zcodemap);
//            }
        }
//        return dataMapPorto;
        return null;
    }
}
