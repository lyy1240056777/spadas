package edu.nyu.dss.similarity.datasetReader;

import edu.nyu.dss.similarity.config.SpadasConfig;
import edu.nyu.dss.similarity.index.IndexBuilder;
import edu.nyu.dss.similarity.statistics.DatasetSizeCounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class TrajectoryReader {
    @Autowired
    private SpadasConfig config;

    @Autowired
    private DatasetSizeCounter datasetSizeCounter;

    @Autowired
    private IndexBuilder indexBuilder;

    /*
     * read the trajectory database, e.g., Porot, T-drive
     */
    public Map<Integer, double[][]> read(File file, int limit) throws IOException {
        Map<Integer, double[][]> datasetMap = null;
//        if (config.isCacheDataset())
//            datasetMap = new HashMap<>();
//        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
//            String strLine;
//            int line = 1;
//            while ((strLine = br.readLine()) != null) {
//                String[] splitString = strLine.split(",");
//                double[][] a = new double[splitString.length / 2][];
//                for (int i = 0; i < splitString.length; i++) {
//                    if ((i + 1) % 2 == 0) {
//                        a[i / 2] = new double[2];
//                        for (int j = i - 1; j < i + 1; j++) {
//                            a[i / 2][j - i + 1] = Double.parseDouble(splitString[j]);
//                        }
//                    }
//                }
//                datasetSizeCounter.put(splitString.length / 2);
//                if (config.isCacheDataset())
//                    datasetMap.put(line, a);
//                if (config.isCacheIndex()) {
//                    indexBuilder.createDatasetIndex(line, a);
//                }
//                indexBuilder.storeZcurve(a, line);
//                line++;
//                // FIXME: I don't know why there is a magic number.
//                int NumberQueryDataset = 1;
//                if (line > limit + NumberQueryDataset)
//                    break;
//            }
//        }
        return datasetMap;
    }
}
