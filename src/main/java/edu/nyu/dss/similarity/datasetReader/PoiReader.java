package edu.nyu.dss.similarity.datasetReader;

import edu.nyu.dss.similarity.CityNode;
import edu.nyu.dss.similarity.index.DataMapPorto;
import edu.nyu.dss.similarity.index.DatasetIDMapping;
import edu.nyu.dss.similarity.index.FileIDMap;
import edu.nyu.dss.similarity.index.IndexBuilder;
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
import java.nio.file.Files;
import java.util.Map;
import java.util.stream.Stream;

@Component
public class PoiReader {

    @Value("${spadas.dimension}")
    private int dimension;

    @Value("${spadas.cache-dataset}")
    private boolean cacheDataset;

    @Value("${spadas.cache-index}")
    private boolean cacheIndex;

    @Autowired
    private DatasetSizeCounter datasetSizeCounter;

    @Autowired
    private PointCounter pointCounter;

    @Autowired
    private FileIDMap fileIDMap;

    @Autowired
    private DataMapPorto dataMapPorto;

    @Autowired
    private DatasetIDMapping datasetIDMapping;

    @Autowired
    private IndexBuilder indexBuilder;

    /*
     * read single dataset in a poi folder
     */
    public Map<Integer, double[][]> read(File file, int fileNo, CityNode cityNode) throws IOException {
        if (!file.getName().endsWith("csv")) {
            return null;
        }
        long lineNumber = 0;
        try (Stream<String> lines = Files.lines(file.toPath())) {
            lineNumber = lines.count();
        }
        double[][] a = new double[(int) lineNumber - 1][];
        int i = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String strLine;
            while ((strLine = br.readLine()) != null) {
                String[] splitString = strLine.split(",");
                String aString = splitString[0];
                if (aString.matches("-?\\d+(\\.\\d+)?")) {// only has float
                    a[i] = new double[dimension];
                    for (int j = 0; j < dimension; j++) {
                        a[i][j] = Double.parseDouble(splitString[i]);
                    }
                    i++;
                }
            }
        }
        if (i > 0) {
            // 统计数据集 size 分布
            datasetSizeCounter.put(i);
            if (cacheDataset)
                dataMapPorto.put(fileNo, a);
            if (cacheIndex) {
                indexNode node = indexBuilder.createDatasetIndex(fileNo, a, 1, cityNode);
                node.setFileName(file.getName());
                indexBuilder.samplingDataByGrid(a, fileNo, node);
            }
            datasetIDMapping.put(fileNo, file.getName());
            fileIDMap.put(fileNo, file);
//          storeZcurve(a, fileNo, 0, 0, 0, 0, null);
        }
        return dataMapPorto;
    }
}
