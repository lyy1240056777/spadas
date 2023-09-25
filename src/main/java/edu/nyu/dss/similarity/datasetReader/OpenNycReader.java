package edu.nyu.dss.similarity.datasetReader;

import edu.nyu.dss.similarity.CityNode;
import edu.nyu.dss.similarity.config.SpadasConfig;
import edu.nyu.dss.similarity.index.*;
import edu.nyu.dss.similarity.statistics.DatasetSizeCounter;
import edu.nyu.dss.similarity.statistics.PointCounter;
import edu.rmit.trajectory.clustering.kmeans.indexNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
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
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class OpenNycReader {
    @Autowired
    private SpadasConfig config;

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


    @Autowired
    private DatasetPerDir datasetPerDir;

    /**
     * 读取 Open NYC 的数据集
     * <p>
     * 有多少数据集有坐标的（195:325）
     *
     * @param file     文件对象
     * @param id       文件编号
     * @param cityNode 城市节点
     * @return 读取到的数据行数
     * @throws IOException
     */
    public int read(File file, int id, CityNode cityNode) throws IOException {
        if (!file.getName().endsWith(".csv")) {
            return 0;
        }
        log.info("reading {}", file.getName());
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            CSVParser parser = CSVFormat.DEFAULT.withHeader().parse(reader);
            Map<String, Integer> headers = parser.getHeaderMap();
            String latKey = ReaderUtils.findLatKeyByHeader(headers);
            String lonKey = ReaderUtils.findLonKeyByHeader(headers);
            if (parser.getRecordNumber() >= Integer.MAX_VALUE) {
                throw new RuntimeException(String.format("Too many rows in file %s.", file.getName()));
            }
            if (latKey.isEmpty() || lonKey.isEmpty()) {
                log.info("[open-nyc]no position in file {}", file.getName());
                return 0;
            }
            List<CSVRecord> records = parser.getRecords();
            List<double[]> spatialData = new ArrayList<>();
            AtomicInteger size = new AtomicInteger();
            records.forEach(record -> {
                String lat = record.get(latKey);
                String lon = record.get(lonKey);
                if (lat.isEmpty() || lon.isEmpty()) {
                    return;
                }
                try {
                    double[] position = new double[2];
                    position[0] = Double.parseDouble(lat);
                    position[1] = Double.parseDouble(lon);
                    spatialData.add(position);
                    size.addAndGet(1);
                } catch (NumberFormatException e) {
                    log.warn("Error format position column in file {} line {}", file.getName(), size.intValue());
                }
            });
            pointCounter.put(spatialData.size());
            datasetSizeCounter.put(spatialData.size());
            loadSpatialIndex(spatialData.toArray(new double[size.intValue()][]), id, file, cityNode);
            return spatialData.size();
        } catch (IOException e) {
            log.error(e.getMessage());
            throw e;
        }
    }


    private void loadSpatialIndex(double[][] spatialData, int id, File file, CityNode cityNode) {
        if (config.isCacheDataset()) {
            dataMapPorto.put(id, spatialData);
        }
        if (config.isCacheIndex()) {
            indexNode node = indexBuilder.createDatasetIndex(id, spatialData, 1, cityNode);
            indexBuilder.samplingDataByGrid(spatialData, id, node);
            node.setFileName(file.getParent());
        }
        datasetIDMapping.put(id, file.getName());
        fileIDMap.put(id, file);
    }
}
