package edu.nyu.dss.similarity.datasetReader;

import edu.nyu.dss.similarity.CityNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

@Component
@Slf4j
public class OpenNycReader {

    /**
     * 读取 Open NYC 的数据集
     * <p>
     * 先试试到底有多少数据集有坐标的（195/325）还可以
     *
     * @param file     文件对象
     * @param fileNo   文件编号
     * @param filename 文件名
     * @param cityNode 城市节点
     * @return
     * @throws IOException
     */
    public Map<Integer, double[][]> read(File file, int fileNo, String filename, CityNode cityNode) throws IOException {
        if (!filename.endsWith(".csv")) {
            return null;
        }
        log.info("reading {}", file.getName());
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            CSVParser parser = CSVFormat.DEFAULT.withHeader().parse(reader);
            Map<String, Integer> headers = parser.getHeaderMap();
            if (headers.containsKey("lat") || headers.containsKey("latitude")) {
                log.info("[open-nyc]we get position in file {}", filename);
            } else {
                log.info("[open-nyc]no position in file {}", filename);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            throw e;
        }
        return null;
    }
}
