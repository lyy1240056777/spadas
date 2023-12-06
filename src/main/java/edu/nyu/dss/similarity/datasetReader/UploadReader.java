package edu.nyu.dss.similarity.datasetReader;

import edu.nyu.dss.similarity.config.SpadasConfig;
import edu.rmit.trajectory.clustering.kmeans.IndexAlgorithm;
import edu.rmit.trajectory.clustering.kmeans.IndexNode;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class UploadReader {

    @Autowired
    private SpadasConfig config;

    @Autowired
    private IndexAlgorithm indexAlgo;

    public Pair<IndexNode, double[][]> read(MultipartFile file, String filename) throws IOException {
        if (!file.getContentType().equals("text/csv")) {
            return null;
        }
        List<String> headers = new ArrayList<>();
        List<double[]> list = new ArrayList<>();
        double[][] xxx;
        int lat = 0, lng = 0;

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                String[] splitString = strLine.split(",");
                if (headers.isEmpty() && lat == 0) {
                    Collections.addAll(headers, splitString);
                    if (!headers.contains("lat") || !headers.contains("lng")) {
                        return null;
                    }
                    lat = headers.indexOf("lat");
                    lng = headers.indexOf("lng");
                } else {
                    double[] data = new double[2];
                    if (splitString.length < lat + 1 || splitString.length < lng + 1) {
                        continue;
                    }
                    data[0] = Double.parseDouble(splitString[lat]);
                    data[1] = Double.parseDouble(splitString[lng]);
                    if (data[0] < -90 || data[0] > 90 || data[1] < -180 || data[1] > 180) {
                        continue;
                    }
//                除掉经纬度都为0的点（我就不信有这么巧）
                    if (data[0] == 0 && data[1] == 0) {
                        continue;
                    }
                    list.add(data);
                }
            }
            xxx = list.toArray(new double[list.size()][]);
//            System.out.println("File " + file.getName() + " has " + list.size() + " lines");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (list.isEmpty()) {
            return null;
        }
        IndexNode node = buildNode(xxx);
        node.setFileName(filename);
        node.setDatasetID(-3);
        return new MutablePair<>(node, xxx);
    }


    /*-*
	build node and not insert
	 */
    private IndexNode buildNode(double[][] data) {
        IndexNode newNode = indexAlgo.buildBalltree2(data, config.getDimension(), config.getDimension(), null, null);
        return newNode;
    }
}
