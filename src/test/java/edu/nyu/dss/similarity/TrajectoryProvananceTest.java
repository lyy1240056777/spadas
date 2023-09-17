package edu.nyu.dss.similarity;


import edu.nyu.dss.similarity.datasetReader.TrajectoryReader;
import edu.rmit.trajectory.clustering.kpaths.Util;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import web.SpadasWebApplication;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpadasWebApplication.class)
public class TrajectoryProvananceTest {

    @Autowired
    private TrajectoryReader trajectoryReader;

    /*
     * create a set of dataset with map matching, sampling, simplication (ratio), enrichment (ratio), outlier removal (far points),
     * segmentation (ratio)
     * Input: trajectory dataset, output: a new file with multiple operations, and combinations (multiple).
     */
    static void cleanDataset(Map<Integer, double[][]> trajectoryDataset, Map<Integer, double[][]> mappedtrajectoryDataset, int datasetThreshold, String folder) {
        int i = 1;
        String fileString = "";
        int sampingRatio = 5;
        for (int trajectoryid : trajectoryDataset.keySet()) {
            int fileno = (int) i / datasetThreshold;
            String fielnameString = folder + fileno + ".txt";
            String mappingfielnameString = folder + fileno + "mm.txt";
            String samplefielnameString = folder + fileno + "sample.txt";
            String simplynameString = folder + fileno + "remove.txt";
            String enrichnameString = folder + fileno + "enrich.txt";

            fileString = "";
            double[][] trajectory = trajectoryDataset.get(trajectoryid);
            for (int j = 0; j < trajectory.length; j++)
                fileString += "-" + trajectory[j][0] + "," + trajectory[j][1] + ",";
            Util.write(fielnameString, fileString + "\n");
            Util.write(mappingfielnameString, mapmatching(mappedtrajectoryDataset.get(trajectoryid)) + "\n");
            Util.write(simplynameString, simplifying(trajectory, 2) + "\n");
            Util.write(enrichnameString, enrichment(trajectory, 2) + "\n");
            if ((int) i % datasetThreshold % sampingRatio == 0)
                Util.write(samplefielnameString, fileString + "\n");
            // conduct the cleaning and storing into multiple files
            // store label in the title
            i++;
        }
    }

    static String mapmatching(double[][] trajectory) {
        String fileString = "";
        for (int j = 0; j < trajectory.length; j++)
            fileString += trajectory[j][0] + "," + trajectory[j][1] + ",";
        return fileString;
    }

    static String simplifying(double[][] trajectory, int ratio) {
        String fileString = "";
        for (int j = 0; j < trajectory.length; j++) {
            if (j % ratio == 0)
                fileString += "-" + trajectory[j][0] + "," + trajectory[j][1] + ",";
        }
        return fileString;
    }

    static String enrichment(double[][] trajectory, int ratio) {
        String fileString = "";
        for (int j = 0; j < trajectory.length; j++) {
            fileString += "-" + trajectory[j][0] + "," + trajectory[j][1] + ",";
            if (j == trajectory.length - 1)
                continue;
            double rangex = trajectory[j][0] - trajectory[j + 1][0];
            double rangey = trajectory[j][1] - trajectory[j + 1][1];
            for (int i = 1; i < ratio; i++) {
                double x = trajectory[j][0] + rangex / ratio * i;
                double y = trajectory[j][1] + rangey / ratio * i;
                fileString += "-" + x + "," + y + ",";
            }
        }
        return fileString;
    }

    @Test
    public void testTrajectory(String[] args) throws IOException {
        // TODO Auto-generated method stub
        File dataFile = new File(args[0]);
        File mappdataFile = new File(args[1]);

        String edgeFile = args[3];
        String edgedatasetFile = args[4];
        String writeedgedatasetFile = args[5];
        String writenodedatasetFile = args[6];
        //	convertEdge(edgeFile, edgedatasetFile, writeedgedatasetFile, writenodedatasetFile);
        mappdataFile = new File(writenodedatasetFile);
        Map<Integer, double[][]> trajectory = trajectoryReader.read(dataFile, 10000000);
        Map<Integer, double[][]> mapped = trajectoryReader.read(mappdataFile, 10000000);
        cleanDataset(trajectory, mapped, 1000, args[2]);
    }
}
