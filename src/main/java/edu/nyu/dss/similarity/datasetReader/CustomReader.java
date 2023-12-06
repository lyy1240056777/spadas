package edu.nyu.dss.similarity.datasetReader;

import edu.nyu.dss.similarity.index.DataMapPorto;
import edu.nyu.dss.similarity.index.DatasetIDMapping;
import edu.nyu.dss.similarity.index.IndexBuilder;
import edu.nyu.dss.similarity.statistics.PointCounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Stream;

@Deprecated
@Component
public class CustomReader {

    @Autowired
    private PointCounter pointCounter;

    @Value("${spadas.cache-index}")
    private boolean cacheIndex;

    @Value("${spadas.cache-dataset}")
    private boolean cacheDataset;

    @Autowired
    private DataMapPorto dataMapPorto;
    @Autowired
    private IndexBuilder indexBuilder;

    @Autowired
    private DatasetIDMapping datasetIDMapping;

    public void read(File file, int fileNo) throws IOException {
        long lineNumber;
        try (Stream<String> lines = Files.lines(file.toPath())) {
            lineNumber = lines.count();
        }
        double[][] a = new double[(int) lineNumber - 1][];// a little different
        int i = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String strLine;
            while ((strLine = br.readLine()) != null) {
                String[] splitString = strLine.split(",");
                if (splitString.length < 3)
                    System.out.println(file.toURI());
                String aString = splitString[3];
                if (aString.matches("-?\\d+(\\.\\d+)?")) {// only has float
                    a[i] = new double[3];
                    a[i][0] = Double.parseDouble(splitString[6]);
                    a[i][1] = Double.parseDouble(splitString[7]);
                    a[i][2] = Double.parseDouble(splitString[1].replace("-", ""));
                    i++;
                }
            }
        }
        pointCounter.put(a.length);

        if (cacheDataset)
            dataMapPorto.put(fileNo, a);
        if (cacheIndex) {
//            indexBuilder.createDatasetIndex(fileNo, a, 0, datasetIDMapping.get(fileNo));
        }
//        if (!zcurveExist)
//            storeZcurve(a, fileNo);
    }
}
