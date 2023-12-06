package edu.nyu.dss.similarity.index;

import edu.nyu.dss.similarity.CityNode;
import edu.nyu.dss.similarity.EffectivenessStudy;
import edu.whu.config.SpadasConfig;
import edu.rmit.trajectory.clustering.kmeans.IndexAlgorithm;
import edu.rmit.trajectory.clustering.kmeans.IndexNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Component
public class IndexBuilder {
    @Autowired
    private SpadasConfig config;

    @Autowired
    private IndexAlgorithm indexDSS;

    @Autowired
    public IndexNodes indexNodes;

    @Autowired
    private DataSamplingMap dataSamplingMap;

    @Autowired
    public IndexMap indexMap;

    @Autowired
    private ZCodeMap zCodeMap;

    @Autowired
    private ZCodeMapEmd zCodeMapEmd;

    private boolean buildOnlyRoots = false;

    @Deprecated
    private double[] weight = null;

    @Deprecated
    private final String indexString = "index/";

    @Deprecated
    static double minx = -90, miny = -180;
    //    初试设为了4600，为什么？
//    说到底这个spaceRange到底有什么用
    @Deprecated
    static double spaceRange = 100;

    public IndexNode createDatasetIndex(int a, double[][] dataset, int type, CityNode cityNode) {
        IndexNode rootBall;
        if (buildOnlyRoots) {// just create the root node, for datalake creation（只建下层索引的根节点，作为上层索引的叶子节点）
            rootBall = createRootsDataset(dataset, config.getDimension());
        } else {// create the full tree
//            核心核心核心算法，建树
            rootBall = indexDSS.buildBalltree2(dataset, config.getDimension(), config.getLeafCapacity(), null, null, weight);
            rootBall.setType(type);
            String indexFileName = indexString + a + ".txt";
            File tempFile = new File(indexFileName);
            if (!tempFile.exists() && config.isSaveIndex())//&& !aString.contains("porto") && !aString.contains("beijing")
                indexDSS.storeIndex(rootBall, 1, indexFileName, 0);
            if (indexMap == null)
                indexMap = new IndexMap();
            indexMap.put(a, rootBall);

            indexNodes.add(rootBall);
            cityNode.nodeList.add(rootBall);
            cityNode.nodeCount += 1;
        }
        indexDSS.ResetGlobalID();
        rootBall.setroot(a);// set an id to identify which dataset it belongs to
//        if (a < limit) {
//            indexNodes.add(rootBall);
//            cityNode.nodeList.add(rootBall);
//            cityNode.nodeCount += 1;
//        }
        return rootBall;
    }

    /*
     * just create a root node to cover all the points
     */
    private IndexNode createRootsDataset(double[][] dataset, int dimension) {
        double[] max;
        double[] min;
        double[] pivot;
        double radius = 0;
        max = new double[dimension];
        min = new double[dimension];
        for (int i = 0; i < dimension; i++) {
            max[i] = Double.MIN_VALUE;
            min[i] = Double.MAX_VALUE;
        }
        pivot = new double[dimension];
        for (double[] datapoint : dataset) {
            for (int i = 0; i < dimension; i++) {
                if (datapoint[i] > max[i])
                    max[i] = datapoint[i];
                if (datapoint[i] < min[i])
                    min[i] = datapoint[i];
            }
        }
        for (int i = 0; i < dimension; i++) {
            pivot[i] = (max[i] + min[i]) / 2;
            if (weight != null)
                radius += Math.pow((max[i] - min[i]) * weight[i] / 2, 2); // the radius should consider d dimension
            else
                radius += Math.pow((max[i] - min[i]) / 2, 2);
        }
        radius = Math.sqrt(radius);
        IndexNode rootNode = new IndexNode(dimension);
        rootNode.setMBRmax(max);
        rootNode.setMBRmax(min);
        rootNode.setRadius(radius);
        rootNode.setPivot(pivot);
        rootNode.setTotalCoveredPoints(dataset.length);
        return rootNode;
    }


    public void samplingDataByGrid(double[][] data, int id, IndexNode node) {
        double xMin = node.getPivot()[0] - node.getRadius();
        double yMin = node.getPivot()[1] - node.getRadius();
        double unit = node.getRadius() * 2 / Math.pow(2, config.getResolution());
        int len = (int) Math.pow(2, config.getResolution());
        HashMap<Integer, Integer> dataSamp = new HashMap<>();
        for (double[] d : data) {
            int xSamp = (int) ((d[0] - xMin) / unit);
            int ySamp = (int) ((d[1] - yMin) / unit);
            int intSamp = len * ySamp + xSamp;
            if (dataSamp.containsKey(intSamp)) {
                int weight = dataSamp.get(intSamp);
                dataSamp.put(intSamp, weight + 1);
            } else {
                dataSamp.put(intSamp, 1);
            }
        }
        List<double[]> dataSampling = new ArrayList<>();
        dataSamp.forEach((k, v) -> {
            double[] tmp = new double[3];
            tmp[0] = (k % len) * unit + xMin;
            tmp[1] = (k / len) * unit + yMin;
            tmp[2] = (double) v / data.length;
//            tmp[2] = v;
            dataSampling.add(tmp);
        });
        dataSamplingMap.put(id, dataSampling);
    }


    //    旧的z-order生成方法，
    public void storeZcurve(double[][] dataset, int datasetid) {
        int numberCells = (int) Math.pow(2, config.getResolution());
        double unit = spaceRange / numberCells;
        ArrayList<Integer> zcodeaArrayList = new ArrayList<>();
        for (double[] doubles : dataset) {
            int x = (int) ((doubles[0] - minx) / unit);
            int y = (int) ((doubles[1] - miny) / unit);
//            图省事，日后要修改，可能导致精度丢失
//            resolution + 1才能保证combine过程不丢失x和y的精度
            int zcode = (int) EffectivenessStudy.combine(x, y, config.getResolution() + 1);
            if (!zcodeaArrayList.contains(zcode))
                zcodeaArrayList.add(zcode);
        }
        Collections.sort(zcodeaArrayList);
        zCodeMap.put(datasetid, zcodeaArrayList);
    }

    /*
     * storing the z-curve
     */
    public void storeZCurveForEMD(double[][] dataset, int datasetId, double xRange, double yRange, double minx, double miny) {
        int pointCnt = dataset.length;
        int numberCells = (int) Math.pow(2, config.getResolution());
        double xUnit = xRange / numberCells;
        double yUnit = yRange / numberCells;
        double weightUnit = 1.0 / pointCnt;
        HashMap<Long, Double> zCodeItemMap = new HashMap<>();
        for (double[] doubles : dataset) {
            int x = (int) ((doubles[0] - minx) / xUnit);
            int y = (int) ((doubles[1] - miny) / yUnit);
            long zCode = EffectivenessStudy.combine(x, y, config.getResolution());
            if (zCodeItemMap.containsKey(zCode)) {
                double val = zCodeItemMap.get(zCode);
                zCodeItemMap.put(zCode, val + weightUnit);
            } else {
                zCodeItemMap.put(zCode, weightUnit);
            }
        }
        zCodeMapEmd.put(datasetId, zCodeItemMap);
    }
}
