package edu.nyu.dss.similarity.datasetReader;

import edu.nyu.dss.similarity.CityNode;
import edu.nyu.dss.similarity.index.*;
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

@Component
public class UsaReader {

    @Autowired
    private DatasetSizeCounter datasetSizeCounter;

    @Autowired
    private PointCounter pointCounter;

    @Autowired
    private DatasetPerDir datasetPerDir;

    @Autowired
    private DataMapPorto dataMapPorto;

    @Autowired
    private DatasetIDMapping datasetIDMapping;

    @Autowired
    private FileIDMap fileIDMap;

    @Autowired
    private IndexBuilder indexBuilder;

    @Value("${spadas.dimension}")
    private int dimension;

    @Value("${spadas.cache-dataset}")
    private boolean cacheDataset;

    @Value("${spadas.cache-index}")
    private boolean cacheIndex;

    @Autowired
    private ZCodeMap zCodeMap;

    public Map<Integer, double[][]> read(File dataFile, int fileNo, CityNode cityNode, int datasetIDForOneDir, String fileName) {
        indexNode node = new indexNode(2);
        int i = 0;
        List<double[]> list = new ArrayList<>();
        double[][] data;
//        解析单个数据集文件并存到数组中
        try (BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
            String strLine;
            while ((strLine = br.readLine()) != null) {
                String[] splitString = strLine.split(",");
                if (splitString.length != 2) {
//                    System.out.println();
                    continue;
                }
                if (splitString[0].isEmpty() || splitString[1].isEmpty()) {
                    continue;
                }
                String aString = splitString[0];
                if (aString.equals("lng")) {
                    continue;
                }
                double[] b = new double[dimension];
                b[0] = Double.parseDouble(splitString[1]);
                b[1] = Double.parseDouble(splitString[0]);
//                会不会造成整个数据集文件都没有有效数据的情况
//                if (b[0] < 25 || b[0] > 50 || b[1] < -130 || b[1] > -70) {
//                    continue;
//                }
                if (b[0] < -90 || b[0] > 90 || b[1] < -180 || b[1] > 180) {
                    continue;
                }
//                除掉经纬度都为0的点（我就不信有这么巧）
                if (b[0] == 0 && b[1] == 0) {
                    continue;
                }
                list.add(b);
                i++;
            }
            data = list.toArray(new double[i][]);
            pointCounter.put(list.size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (i > 0) { // 确保读取到了数据
            datasetSizeCounter.put(i);
            if (cacheDataset) { // 这能是false？
                dataMapPorto.put(fileNo, data);
                datasetPerDir.put(datasetIDForOneDir, data);
            }
            if (cacheIndex) { // true，要创建下层索引（数据集索引）
//				createDatasetIndex(fileNo, xxx,1);
//                创建下层索引，cityNode没有用
                node = indexBuilder.createDatasetIndex(fileNo, data, 1, cityNode);
                node.setFileName(fileName);
//                为了系统前端好显示
                indexBuilder.samplingDataByGrid(data, fileNo, node);
            }
//            一些全部变量
            datasetIDMapping.put(fileNo, fileName);
            fileIDMap.put(fileNo, dataFile);
            indexBuilder.storeZcurve(data, fileNo);
            node.setSignautre(zCodeMap.get(fileNo).stream().mapToInt(Integer::intValue).toArray());
//                EffectivenessStudy.SerializedZcurve(zcodemap);
        }
        return dataMapPorto;
    }
}
