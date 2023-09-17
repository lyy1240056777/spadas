package edu.nyu.dss.similarity;

import edu.nyu.dss.similarity.utils.FileUtil;
import edu.rmit.trajectory.clustering.kmeans.IndexAlgorithm;
import edu.rmit.trajectory.clustering.kmeans.indexNode;
import edu.rmit.trajectory.clustering.kpaths.Util;
import emd.*;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import web.exception.FileException;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Stream;

public class FrameworkBackup {
    /*
     * data set
     */
    static String edgeString, nodeString, distanceString, fetureString, indexString = "";
    static String zcodeSer;


    public static String aString;
    //    叶子节点最大容量，经常要改
    static int capacity = 1000;
    public static Map<Integer, String> datasetIdMapping = new HashMap<>();//integer
    //    存储数据集文件id到数据集文件名的映射，每个目录独立映射

    public static HashMap<Integer, List<double[]>> dataSamplingMap = new HashMap<>();
    public static ArrayList<Map<Integer, String>> datasetIdMappingList = new ArrayList<>();
    public static Map<Integer, double[][]> dataMapPorto = new HashMap<>();
    //    每遍历一个目录文件（如城市）生成一个新的map，value总共组成了dataMapPorto，用于计算emd
    public static Map<Integer, double[][]> dataMapForEachDir = new HashMap<>();
    static TreeMap<Integer, Integer> countHistogram = new TreeMap<>();
    //    维护数据集id到数据集文件路径的映射
    public static Map<Integer, File> fileIDMap = new HashMap<>();

    public static int datalakeID;// the lake id
    static int fileNo = 0;
    //	store the data of every point of the whole data lake
    public static List<double[]> dataPoint = new ArrayList<>();

    public static List<CityNode> cityNodeList = new ArrayList<>();

    public static Map<String, List<indexNode>> cityIndexNodeMap = new HashMap<>();
    /*
     * z-curve for grid-based overlap
     */
//    对于中国地图，lat纬度范围是0-60，lng经度范围是70-140，所以minx设置成0，miny设置成70
//    可能得对每个城市设置特定的参数，不然精度不够
//    static double minx = -90, miny = -180;// the range of dataset space, default values for Argo dataset  TODO now have changed it to lat/lon coordination
    static double minx = -90, miny = -180;
    //    初试设为了4600，为什么？
//    说到底这个spaceRange到底有什么用
    static double spaceRange = 100;
    //    对于中国地图，resolution设置为7或8比较好
//    static int resolution = 3; // also a parameter to test the grid-based overlap, and the approximate hausdorff, and range query
    static int resolution = 10;
    static double error = 0.0;

    /*
     * index
     */
    static IndexAlgorithm indexDSS = new IndexAlgorithm();
    public static Map<Integer, indexNode> indexMap;// root node of dataset's index
    static Map<Integer, indexNode> datalakeIndex = null;// the global datalake index
    public static ArrayList<indexNode> indexNodes = new ArrayList<>(); // store the root nodes of all datasets in the lake
    static ArrayList<indexNode> indexNodesAll;
    static indexNode datasetRoot = null; // the root node of datalake index in memory mode
    static Map<Integer, Map<Integer, indexNode>> datasetIndex; // restored dataset index
    static HashMap<Integer, ArrayList<Integer>> zcodemap = new HashMap<>();
    //    新的z顺序签名哈希表，用来计算emd
    //    对整个数据湖建立z顺序签名哈希表，每个数据集目录独立，用来计算emd
    static ArrayList<HashMap<Integer, HashMap<Long, Double>>> zcodeMapForLake = new ArrayList<>();
    static Map<Integer, Pair<Double, PriorityQueue<queueMain>>> resultPair = new HashMap<>();
    /*
     * dataset features
     */
    static Map<Integer, double[]> featureMap = new HashMap<>(); //storing the features for each map

    /*
     * parameters
     */
    static double[] weight = null; // the weight in all dimensions
    static int dimension = 2;
    static int limit = 100000; //number of dataset to be searched
    static int NumberQueryDataset = 1; // number of query datasets
    static boolean[] dimNonSelected;// indicate which dimension is not selected
    static boolean dimensionAll = true;// indicate that all dimensions are selected.

    static int[] ks = {10, 20, 30, 40, 50};
    static int[] ds = {2, 4, 6, 8, 10};
    static double[] ns = {0.0001, 0.001, 0.01, 0.05, 0.1};// number of datasets indexed
    static int[] ss = {1, 10, 100, 500, 1000}; //number of datasets to be combined together, i.e., the scale of dataset
    static int[] rs = {3, 4, 5, 6, 7};// resolution to verify the range query, grid overlap, approximate hausdorff
    static int[] fs = {10, 20, 30, 40, 50};

    /*
     * settings
     */
    static boolean storeIndexMemory = true;// check whether need to use the index in memeory or load from disk
    static boolean saveDatasetIndex = false;// whether to store each dataset index as file
    static boolean storeAllDatasetMemory = false; // store all the datasets in the datasetPorto
    static boolean zcurveExist = false;
    static boolean buildOnlyRoots = false; // for building fast datalake index
    static boolean testFullDataset = true; //

    /*
     * information to compare all the algorithms
     */
    static int numberofComparisons = 4;// all the comparisons
    static int numberofParameterChoices = 5; //
    static double[][] timeMultiple;// the overall running time
    static double[][] indexMultiple;

    public static PriorityQueue<IndexNodeExpand> PQ_Branch = new PriorityQueue<>(new ComparatorByIndexNodeExpand());
    public static double LB_Branch = 1000000000;
    public static double UB_Branch = 1000000000;

    public static ArrayList<String[]> getPooling(HashMap<Integer, HashMap<Long, Double>> mapForDir) {
        ArrayList<String[]> dataSetList_after_pooling = new ArrayList<>();
        pooling p = new pooling();
        mapForDir.forEach((k, v) -> {
            HashMap<Long, Double> map1 = v;
            signature_t s1 = getSignature(map1);
            signature_t s1_pooling = null;
            try {
                s1_pooling = p.poolingOperature(s1, 1);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
            double ub_move = p.getUb();
            String[] string = new String[4];
            string[0] = String.valueOf(k);
            string[1] = String.valueOf(s1_pooling.Features[0].X);
            string[2] = String.valueOf(s1_pooling.Features[0].Y);
            string[3] = String.valueOf(ub_move);
            dataSetList_after_pooling.add(string);
        });
        return dataSetList_after_pooling;
    }

    public static signature_t getAllData(HashMap<Integer, HashMap<Long, Double>> mapForDir, int datasetQueryID, HashMap<Integer, ArrayList<double[]>> allHistogram,
                                         double[][] iterMatrix, double[] ubMove, int[] histogram_name, double[] query, double queryRadius) {
        ArrayList<double[]> l = new ArrayList<>();
        DoubleArrayList ub = new DoubleArrayList();
        ArrayList<String[]> datasetList_after_pooling = getPooling(mapForDir);
        ArrayList<Integer> his = new ArrayList(); //his coresponding to the all histogram_name
        ArrayList<Integer> datasetID = new ArrayList<>();
        int numberOfLine = 0;
        signature_t querySignature = null;
        while (numberOfLine < mapForDir.size()) {
            ArrayList<double[]> allPoints = new ArrayList<>();
            HashMap<Long, Double> map1 = mapForDir.get(numberOfLine);
            for (long key : map1.keySet()) {
                long[] coord = resolve(key);
                double[] d = new double[3];
                d[0] = Double.parseDouble(String.valueOf(coord[0]));
                d[1] = Double.parseDouble(String.valueOf(coord[1]));
                d[2] = map1.get(key);
                allPoints.add(d);
            }
            //sampleData
            String[] buf = datasetList_after_pooling.get(numberOfLine);
            allHistogram.put(numberOfLine, allPoints);
//            datasetID.add(numberOfLine);
            if (numberOfLine == datasetQueryID) {
                //sampleData
//                query = new double[dimension];
                query[0] = Double.parseDouble(buf[1]);
                query[1] = Double.parseDouble(buf[2]);
                queryRadius = Double.parseDouble(buf[3]);

                int n = allPoints.size();
                feature_t[] features = new feature_t[n];
                double[] weights = new double[n];
                for (int i = 0; i < n; i++) {
                    features[i] = new feature_t(allPoints.get(i)[0], allPoints.get(i)[1]);
                    weights[i] = allPoints.get(i)[2];
                }
                querySignature = new signature_t(n, features, weights);
            }
            //sampleData
            double[] corrd = new double[dimension];
            corrd[0] = Double.parseDouble(buf[1]);
            corrd[1] = Double.parseDouble(buf[2]);
            l.add(corrd);
            his.add(Integer.parseInt(buf[0]));
            ub.add(Double.parseDouble(buf[3]));
            numberOfLine++;
        }
        //sampleData
        int countOfRow = numberOfLine;
//        iterMatrix = new double[countOfRow][dimension];
//        ubMove = new double[countOfRow];
//        histogram_name = new int[countOfRow];
        ubMove = ub.toDoubleArray();
        for (int i = 0; i < countOfRow; i++) {
            iterMatrix[i][0] = l.get(i)[0];
            iterMatrix[i][1] = l.get(i)[1];
            histogram_name[i] = his.get(i);
        }
//        return datasetID;
        return querySignature;
    }


    public static signature_t getSignature(HashMap<Long, Double> map) {
        int n = map.size();
        feature_t[] Features = new feature_t[n];
        double[] Weights = new double[n];
        long[] Coordinates;
        double unit = 1.0;
        int i = 0;
        for (long key : map.keySet()) {
            Coordinates = resolve(key);
            Features[i] = new feature_t(Coordinates[0] * unit, Coordinates[1] * unit);
            Weights[i] = map.get(key);
            i++;
        }
        signature_t s = new signature_t(n, Features, Weights);
        return s;
    }

    public static signature_t getSignature(int id, HashMap<Integer, ArrayList<double[]>> allHistogram) {
        ArrayList<double[]> a = allHistogram.get(id);
        int n = a.size();
        feature_t[] features = new feature_t[n];
        double[] weights = new double[n];
        for (int i = 0; i < n; i++) {
            features[i] = new feature_t(a.get(i)[0], a.get(i)[1]);
            weights[i] = a.get(i)[2];
        }
        signature_t dataSignature = new signature_t(n, features, weights);
        return dataSignature;
    }

    public static long[] resolve(long code) {
        long[] Coordinates = new long[2];
        String str = Long.toBinaryString(code);

        while (str.length() < 2 * resolution) {
            str = "0" + str;
        }

        StringBuilder c = new StringBuilder();
        StringBuilder d = new StringBuilder();

        for (int i = 0; i < str.length(); i++) {
            if (i % 2 == 0)
                c.append(str.charAt(i));
            else
                d.append(str.charAt(i));
        }

        Coordinates[0] = Long.parseLong(c.toString(), 2);
        Coordinates[1] = Long.parseLong(d.toString(), 2);
        return Coordinates;
    }

    public static void BranchAndBound(indexNode root, double[] query) {
        if (root.isLeaf()) {//leaf node
//            System.out.println("distance(root.getPivot(), query)======"+distance(root.getPivot(), query));
//            System.out.println("root.getEMDRadius()====="+root.getEMDRadius());
            double LowerBound = distance(root.getPivot(), query) - root.getEMDRadius();
            double UpperBound = distance(root.getPivot(), query) + root.getEMDRadius();
//            System.out.print(LowerBound < 0);

            IndexNodeExpand in = new IndexNodeExpand(root, LowerBound, UpperBound);
            if (PQ_Branch.isEmpty()) {
                PQ_Branch.add(in);
            } else if (in.getLb() > UB_Branch) {
                //Pruning
            } else if (in.getUb() < LB_Branch) {
//                PQ_Branch.poll();
//                PQ_Branch.add(in);
                while (in.getUb() < LB_Branch) {
                    PQ_Branch.poll();
                    if (!PQ_Branch.isEmpty()) {
                        LB_Branch = PQ_Branch.peek().lb;
                        UB_Branch = PQ_Branch.peek().ub;
                    } else {
                        break;
                    }
                }
                PQ_Branch.add(in);
            } else {
                PQ_Branch.add(in);
            }
            LB_Branch = PQ_Branch.peek().lb;
            UB_Branch = PQ_Branch.peek().ub;
        } else {//internal node
            Set<indexNode> listnode = root.getNodelist();
            for (indexNode aListNode : listnode) {
                double LowerBound = distance(aListNode.getPivot(), query) - aListNode.getEMDRadius();
                double UpperBound = distance(aListNode.getPivot(), query) + aListNode.getEMDRadius();
                if (LowerBound > UB_Branch) {
                }//no sense looking here
                else {// if (LowerBound <= UB_Branch )
                    BranchAndBound(aListNode, query);
                }
            }

        }
    }

    public static double distance(double[] x, double[] y) {
        double d = 0.0;
        for (int i = 0; i < x.length; i++) {
            d += (x[i] - y[i]) * (x[i] - y[i]);
        }
        return Math.sqrt(d);
    }

    public static indexNode createBallTree() throws IOException, CloneNotSupportedException {
//        long startTime = System.currentTimeMillis();
//        int leafThreshold = leaf_Threshold;
//        int maxDepth = max_Depth; //
//        long startTime = System.currentTimeMillis();
//        long endTime1 = System.currentTimeMillis();
//        indexNode in = ball_tree.create(ubMove, iterMatrix, leafThreshold, maxDepth, dimension);
//        long endTime2 = System.currentTimeMillis(); // current time
//        return in;
        return null;
    }

    public static ArrayList<Integer> getBranchAndBoundResultID(indexNode root, double[] query) {
        ArrayList<Integer> resultID = new ArrayList<>();
        BranchAndBound(root, query);
        while (!PQ_Branch.isEmpty()) {
            IndexNodeExpand in = PQ_Branch.poll();
            resultID.addAll(in.getIn().getpointIdList());
//            System.out.println("LB = "+ in.lb+"  UB==  "+in.ub);
        }
        return resultID;
    }

    //    用于union range query操作，获取dataset D中落在range内部的所有点
    public static void getPointsInRange(double[] mbrmax, double[] mbrmin, int id, int dim) {
//        List<double[]> result = new ArrayList<>();
//        result.addAll(Search.UnionRangeQueryForPoints(mbrmax, mbrmin, id, indexNodes.get(id), result, dim, null, true));

    }

    /*-*
	 build node and insert
	 */
    private static indexNode insertNewDSandUpdate(double[][] data, String filename, int dim) throws IOException {
        int id = datasetIdMapping.size() + 1;
        File file = new File(aString + "/" + filename);
        if (!file.exists())
            throw new FileException("File not exists");
        //create indexnode
        indexNode newNode = indexDSS.buildBalltree2(data, dim, capacity, null, null);
        //insert into indexTree
        indexDSS.insertNewDataset(newNode, datasetRoot, capacity, dim);

        //set param for static val
        newNode.setroot(id);
        datasetIdMapping.put(id, filename);
        dataMapPorto.put(id, data);
        indexMap.put(id, newNode);
        indexNodes.add(newNode);
        indexNodesAll.add(newNode);
        return newNode;
    }


    //    这个形参就很有说法，querydata可以是已有的数据集，也可以是用户上传的数据集，但我们并不支持用户上传啊……
    public static List<double[]> UnionRangeQuery(double[][] querydata, int unionDatasetId, int dim) {
        double[] mbrmax = new double[dim];
        double[] mbrmin = new double[dim];
//        初始化
        Arrays.fill(mbrmax, -Double.MAX_VALUE);
        Arrays.fill(mbrmin, Double.MAX_VALUE);
//        给querydata找mbr
        for (double[] row : querydata) {
            for (int i = 0; i < dim; i++) {
                mbrmax[i] = Double.max(mbrmax[i], row[i]);
                mbrmin[i] = Double.min(mbrmin[i], row[i]);
            }
        }
        List<double[]> result = new ArrayList<>();
//        核心算法
//        Search.UnionRangeQueryForPoints(mbrmax, mbrmin, unionDatasetId, indexNodes.get(unionDatasetId), result, dim, null, false);
        Collections.addAll(result, querydata);
        return result;
    }


    public static double[][] getMatrixByDatasetId(int id) {
        return dataMapPorto.get(id);
    }

    public static PriorityQueue<relaxIndexNode> EMD(int dataDirID, int datasetQueryID, int topk) throws CloneNotSupportedException {
        HashMap<Integer, HashMap<Long, Double>> mapForDir = zcodeMapForLake.get(dataDirID);
        HashMap<Long, Double> mapQuery = mapForDir.get(datasetQueryID);
        HashMap<Integer, ArrayList<double[]>> allHistogram = new HashMap<>();
        double[][] iterMatrix = new double[mapForDir.size()][dimension];
        double[] ubMove = new double[mapForDir.size()];
        int[] histogram_name = new int[mapForDir.size()];
//        signature_t querySignature = new signature_t();
        double[] query = new double[dimension];
        double radiusQuery = 0;
        signature_t querySignature = getAllData(mapForDir, datasetQueryID, allHistogram, iterMatrix, ubMove, histogram_name, query, radiusQuery);
        int leafThreshold = 10;
        int maxDepth = 15;
        indexNode ballTree = ball_tree.create(ubMove, iterMatrix, leafThreshold, maxDepth, dimension);
        ArrayList<Integer> firstFlterResult = getBranchAndBoundResultID(ballTree, query);
        PriorityQueue<relaxIndexNode> resultApprox = new PriorityQueue<>(new ComparatorByRelaxIndexNode());
        relax_EMD re = new relax_EMD();

        int filterCount = 0;
        int refineCount = 0;

        for (int id : firstFlterResult) {
            signature_t data = getSignature(id - 1, allHistogram);
            double emdLB = re.tighter_ICT(data, querySignature);
            if (resultApprox.size() < topk) {
                relaxIndexNode in = new relaxIndexNode(id, emdLB);
                resultApprox.add(in);
                refineCount++;
            } else {
                double best = resultApprox.peek().lb;
                double lowerbound = emdLB;
                if (lowerbound >= best) {
                    filterCount++;
                    continue;
                }//被过滤
                else {
                    relaxIndexNode in = new relaxIndexNode(id, emdLB);
                    resultApprox.poll();
                    resultApprox.add(in);
                    refineCount++;
                }
            }
        }
        System.out.println("query dataset is " + datasetIdMappingList.get(dataDirID).get(datasetQueryID));
        System.out.println("EMD result:");
        System.out.println("top " + topk + " results:");
        for (relaxIndexNode item : resultApprox) {
            System.out.println("id = " + item.resultId + ", name = " + datasetIdMappingList.get(dataDirID).get(item.resultId));
        }
        System.out.println("EMD finished");
        return resultApprox;
    }

    //    id从datasetIdMappingList转换到datasetIdMapping
    public static int convertID(int idOfDir, int idInDir) {
        if (!datasetIdMapping.isEmpty() && !datasetIdMappingList.isEmpty()) {
            int tmp = 0;
            for (int i = 0; i < idOfDir; i++) {
                tmp += datasetIdMappingList.get(i).size();
            }
            return tmp + idInDir;
        }
        System.out.println("convert id failed");
        return -1;
    }

    //    id从datasetIdMapping转换到datasetIdMappingList
    public static int[] convertID(int idOfLake) {
        if (!datasetIdMapping.isEmpty() && !datasetIdMappingList.isEmpty()) {
            int tmp = idOfLake, dirSize = datasetIdMappingList.get(0).size();
            int idOfDir = 0, idInDir = 0;
            while (tmp >= dirSize) {
                tmp -= dirSize;
                idOfDir++;
                dirSize = datasetIdMappingList.get(idOfDir).size();
            }
            idInDir = tmp;
            return new int[]{idOfDir, idInDir};
        }
        System.out.println("convert id failed");
        return null;
    }


    public static void testEMD() throws CloneNotSupportedException {
        int dataDirID = 1, datasetQueryID = 3, topk = 10;
        EMD(dataDirID, datasetQueryID, topk);

    }

    /*
     * compute the normalization weight other dimensions based on the latitude (x) and longitude (y),
     * where the x and y indicate the position of latitude and longitude
     */
    static double[] normailizationWeight(Map<Integer, double[][]> datasetMap, int x, int y, int dimension, String filename) {
        double weight[] = new double[dimension];
        File tempFile = new File(filename);
        File tempFileRange = new File(filename + "range");
        if (tempFile.exists() && tempFileRange.exists()) {// if the serialization file exists, read the weights directly
            HashMap<Integer, Double> weightHashMap = EffectivenessStudy.deSerializationResult(filename);
            HashMap<Integer, Double> xyrange = EffectivenessStudy.deSerializationResult(filename + "range");
            for (int i = 0; i < dimension; i++) {
                weight[i] = weightHashMap.get(i);
            }
            minx = xyrange.get(1);
            miny = xyrange.get(2);
            spaceRange = xyrange.get(3);
        } else {
            HashMap<Integer, Double> weightHashMap = new HashMap<>();
            double max[] = new double[dimension];
            double min[] = new double[dimension];
            double range[] = new double[dimension];
            for (int i = 0; i < dimension; i++) {
                max[i] = Double.MIN_VALUE;
                min[i] = Double.MAX_VALUE;
            }
            for (int datasetid : datasetMap.keySet()) {
                double[][] dataset = datasetMap.get(datasetid);
                for (double[] datapoint : dataset) {
                    for (int i = 0; i < dimension; i++) {
                        if (datapoint[i] > max[i])
                            max[i] = datapoint[i];
                        if (datapoint[i] < min[i])
                            min[i] = datapoint[i];
                    }
                }
            }
            for (int i = 0; i < dimension; i++) {
                range[i] = max[i] - min[i];
                //	System.out.println(max[i]+","+min[i]+","+range[i]+","+datasetMap.keySet().size());
            }
            minx = min[x];
            miny = min[y];
            spaceRange = Math.max(range[x], range[y]);
            HashMap<Integer, Double> xyrange = new HashMap<>();
            xyrange.put(1, minx);
            xyrange.put(2, miny);
            xyrange.put(3, spaceRange);
            edu.nyu.dss.similarity.EffectivenessStudy.SerializedResultTable(filename + "range", xyrange);
            double scale = (range[x] + range[y]) / 2;
            for (int i = 0; i < dimension; i++) {
                if (i != x && i != y) {
                    weightHashMap.put(i, scale / range[i]);
                    weight[i] = scale / range[i];
                } else {
                    weightHashMap.put(i, 1.0);// storing the weights for normalization anywhere
                    weight[i] = 1;
                }
            }
            EffectivenessStudy.SerializedResultTable(filename, weightHashMap);    // no need to store another file but just the weight, serialize into mapping table.
        }
        return weight;// we will use the weight in the index building and search where distance computation is needed for radius
    }


    /*
     * read from the mnist dataset and comparing with the dataset
     */
    public static Map<Integer, double[][]> readMnist(File file) throws IOException {
        Map<Integer, double[][]> datasetMap = new HashMap<Integer, double[][]>();
        //	System.out.println("read file " + file.getCanonicalPath());
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String strLine;
            int line = 1;
            while ((strLine = br.readLine()) != null) {
                // System.out.println(strLine);
                String[] splitString = strLine.split(" ");
                double[][] a = new double[28][];
                for (int i = 0; i < splitString.length; i++) {
                    if ((i + 1) % 28 == 0) {
                        a[i / 28] = new double[28];
                        for (int j = i - 27; j < i + 1; j++) {
                            //	System.out.println(j-i+27);
                            a[i / 28][j - i + 27] = Double.valueOf(splitString[j]);
                        }
                    }
                }
                datasetMap.put(line, a);
                line++;
            }
        }
        return datasetMap;
    }

    /*
     * read from the mnist dataset and comparing with the dataset
     */
    public static Map<Integer, double[][]> readMnist2D(File file) throws IOException {
        Map<Integer, double[][]> datasetMap = new HashMap<Integer, double[][]>();
        System.out.println("read file " + file.getCanonicalPath());
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String strLine;
            int line = 1;
            while ((strLine = br.readLine()) != null) {
                // System.out.println(strLine);
                String[] splitString = strLine.split(" ");
                double[][] a = new double[28][];
                for (int i = 0; i < splitString.length; i++) {
                    if ((i + 1) % 28 == 0) {
                        a[i / 28] = new double[28];
                        for (int j = i - 27; j < i + 1; j++) {
                            //	System.out.println(j-i+27);
                            a[i / 28][j - i + 27] = Double.valueOf(splitString[j]);
                        }
                    }
                }
                datasetMap.put(line, a);
                line++;
            }
        }
        return datasetMap;
    }

    public static void testTriangleFrechet() throws IOException {
        String foldername = "/Users/sw160/Desktop/argoverse-api/dataset/train/data";
        File folder = new File(foldername);
//		debug in the future
        Map<Integer, double[][]> dataMap;
        folder = new File("/Users/sw160/Downloads/torch-clus/dataset/minist_60000_784");
        dataMap = readMnist(folder);
        for (int a : dataMap.keySet()) {
            if (a > dataMap.size() - 3)
                break;
            //	System.out.println(dataMap.get(a)[0].length);
            double distance = Hausdorff.Frechet(dataMap.get(1), dataMap.get(a), dataMap.get(a)[0].length);
            //	System.out.println(distance);
            double distance1 = Hausdorff.Frechet(dataMap.get(a), dataMap.get(a + 1), dataMap.get(a)[0].length);
            double distance2 = Hausdorff.Frechet(dataMap.get(a + 1), dataMap.get(a + 2), dataMap.get(a)[0].length);
            double distance3 = Hausdorff.Frechet(dataMap.get(a), dataMap.get(a + 2), dataMap.get(a)[0].length);
            if (distance1 + distance2 < distance3 || Math.abs(distance1 - distance2) > distance3)
                System.out.println("no triangle inequality");//
        }
    }


    /*
     * get the count of nodes in the lightweight tree,
     */
    static long getDatasetIndexSize(indexNode root, boolean centorid) {
        if (root == null)
            return 0;
        if (root.isLeaf()) {
            return root.getMemorySpadas(dimension, centorid);
        } else {
            Set<indexNode> listnode = root.getNodelist();
            long max = 0;
            for (indexNode aIndexNode : listnode) {
                max += aIndexNode.getMemorySpadas(dimension, centorid);
                max += getDatasetIndexSize(aIndexNode, centorid);
            }
            return max;
        }
    }

    /*
     * test whether hausdorff is metric
     */
    public void testTriangle() throws IOException {
        String foldername = "/Users/sw160/Desktop/argoverse-api/dataset/train/data";
        File folder = new File(foldername);
//		debug in the future
        Map<Integer, double[][]> dataMap;
        folder = new File("/Users/sw160/Downloads/torch-clus/dataset/minist_60000_784");
        dataMap = readMnist(folder);
        for (int a : dataMap.keySet()) {
            if (a > dataMap.size() - 3)
                break;
            //	System.out.println(dataMap.get(a)[0].length);
            double distance = Hausdorff.HausdorffExact(dataMap.get(1), dataMap.get(a), dataMap.get(a)[0].length, false);
            double distance1 = Hausdorff.HausdorffExact(dataMap.get(a), dataMap.get(a + 1), dataMap.get(a)[0].length, false);
            double distance2 = Hausdorff.HausdorffExact(dataMap.get(a + 1), dataMap.get(a + 2), dataMap.get(a)[0].length, false);
            double distance3 = Hausdorff.HausdorffExact(dataMap.get(a), dataMap.get(a + 2), dataMap.get(a)[0].length, false);
            if (distance1 + distance2 < distance3)
                System.out.println("no triangle inequality");//
        }
    }

    public static void generateGTforPrediciton(String distanceString, Map<Integer, double[][]> dataMapPorto, Map<Integer, indexNode> indexMap, int dimension, int limit) {
        FileUtil.write(distanceString, "datasetID,dataset2ID,distance,normalizedDistance\n");
        for (int b : dataMapPorto.keySet()) {// we can build index to accelerate
            if (b > limit)
                break;
            for (int a : dataMapPorto.keySet()) {// we can build index to accelerate
                if (a > limit)
                    break;
                //		double distance = Hausdorff.HausdorffExact(dataMapPorto.get(b), dataMapPorto.get(a),
                //				dataMapPorto.get(a)[0].length, false);

                double distance = AdvancedHausdorff.IncrementalDistanceDirected(dataMapPorto.get(b), dataMapPorto.get(a), dimension, indexMap.get(b), indexMap.get(a), 0, 0, 0.05, null, null, null, true);
                // we should change it to our fast one, instead of this slow
                // building index for each dataset
                double pivot_distance = Util.EuclideanDis(indexMap.get(b).getPivot(), indexMap.get(a).getPivot(),
                        dimension);
                double max_distance = pivot_distance + indexMap.get(b).getRadius() + indexMap.get(a).getRadius();
                FileUtil.write(distanceString, Integer.toString(b) + "," + Integer.toString(a) + "," + Double.toString(distance) + ","
                        + Double.toString((max_distance - distance) / max_distance) + "\n");
            }
        }
    }

    public static void SerializedMappingTable(String file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(datasetIdMapping);
            oos.close();
            fos.close();
            System.out.printf("Serialized HashMap data is saved in hashmap.ser");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /*
     * put the searched files into a folder
     */
    public static void UnionFile(String datasetid, String destination) throws FileNotFoundException, IOException {
        File file = new File(aString + "/" + datasetid);
        File dest = new File(destination + "/" + datasetid);
        try {
            FileUtils.copyFile(file, dest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
     * get the count of nodes in the lightweight tree,
     */
    static long getDatalakeIndexSize(indexNode root, boolean centorid) {
        if (root == null)
            return 0;
        if (root.isrootLeaf()) {
            return root.getMemorySpadas(dimension, centorid);
        } else {
            Set<indexNode> listnode = root.getNodelist();
            long max = 0;
            for (indexNode aIndexNode : listnode) {
                max += aIndexNode.getMemorySpadas(dimension, centorid);
                max += getDatalakeIndexSize(aIndexNode, centorid);
            }
            return max;
        }
    }

    /*
     * get the count of nodes in the lightweight tree,
     */
    static long getDatalakeIndexSizeDisk(int dimension) {
        if (datalakeIndex == null)
            return 0;
        else {
            return datalakeIndex.size() * datalakeIndex.get(1).getMemorySpadas(dimension, null);
        }
    }

    static long getDatalakeSize(int dimension) {
        if (dataMapPorto == null) {
            return 0;
        } else {
            long size = 0;
            for (int datasetid : dataMapPorto.keySet()) {
                double[][] dataset = dataMapPorto.get(datasetid);
                size += dataset.length * dimension * 8;
            }
            return size;
        }
    }

    /*
     * get the count of nodes in the lightweight tree,
     */
    static long getDatasetIndexSizeDisk() {
        if (datasetIndex == null)
            return 0;
        else {
            long size = 0;
            for (int datasetid : datasetIndex.keySet()) {
                Map<Integer, indexNode> nodeMap = datasetIndex.get(datasetid);
                size += nodeMap.size() * nodeMap.get(1).getMemorySpadas(dimension, null);
            }
            return size;
        }
    }

    static long getAllDatasetIndexSize() {
        long max = 0;
        if (indexMap != null)
            for (int i : indexMap.keySet()) {
                indexNode root = indexMap.get(i);
                max += getDatasetIndexSize(root, false);
            }
        return max;
    }


    /*
     * just create a root node to cover all the points
     */
    static indexNode createRootsDataset(double[][] dataset, int dimension, int datasetid) {
        double max[], min[], pivot[], radius = 0;
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
        indexNode rootNode = new indexNode(dimension);
        rootNode.setMBRmax(max);
        rootNode.setMBRmax(min);
        rootNode.setRadius(radius);
        rootNode.setPivot(pivot);
        rootNode.setTotalCoveredPoints(dataset.length);
        return rootNode;
    }


    // create index for a single dataset in a datalake
    static void createDatasetIndex(int a, double[][] dataset) {
        indexNode rootBall;
        if (buildOnlyRoots) {// just create the root node, for datalake creation
            rootBall = createRootsDataset(dataset, dimension, a);
        } else {// create the full tree
            rootBall = indexDSS.buildBalltree2(dataset, dimension, capacity, null, null, weight);
            String indexFileName = indexString + String.valueOf(a) + ".txt";
            File tempFile = new File(indexFileName);
            if (!tempFile.exists() && saveDatasetIndex)//&& !aString.contains("porto") && !aString.contains("beijing")
                indexDSS.storeIndex(rootBall, 1, indexFileName, 0);
            if (indexMap == null)
                indexMap = new HashMap<>();
            indexMap.put(a, rootBall);
        }
        indexDSS.setGloabalid();
        rootBall.setroot(a);// set an id to identify which dataset it belongs to
        if (a < limit)
            indexNodes.add(rootBall);
    }

    static indexNode createDatasetIndex(int a, double[][] dataset, int type, CityNode cityNode) {
        indexNode rootBall;
        if (buildOnlyRoots) {// just create the root node, for datalake creation（只建下层索引的根节点，作为上层索引的叶子节点）
            rootBall = createRootsDataset(dataset, dimension, a);
        } else {// create the full tree
//            核心核心核心算法，建树
            rootBall = indexDSS.buildBalltree2(dataset, dimension, capacity, null, null, weight);
            rootBall.setType(type);
            String indexFileName = indexString + String.valueOf(a) + ".txt";
            File tempFile = new File(indexFileName);
            if (!tempFile.exists() && saveDatasetIndex)//&& !aString.contains("porto") && !aString.contains("beijing")
                indexDSS.storeIndex(rootBall, 1, indexFileName, 0);
            if (indexMap == null)
                indexMap = new HashMap<>();
            indexMap.put(a, rootBall);

            indexNodes.add(rootBall);
            cityNode.nodeList.add(rootBall);
            cityNode.nodeCount += 1;
        }
        indexDSS.setGloabalid();
        rootBall.setroot(a);// set an id to identify which dataset it belongs to
//        if (a < limit) {
//            indexNodes.add(rootBall);
//            cityNode.nodeList.add(rootBall);
//            cityNode.nodeCount += 1;
//        }
        return rootBall;
    }

    /*
     * generate the features for index
     */
    static void generateIndexFeatures() throws IOException {
        double[][] dataset = new double[1917494][300];
        edgeString = "./index/dss/glove_edge.txt";
        nodeString = "./index/dss/glove_node.txt";
        String fileString = "/Users/sw160/Desktop/glove.42B.300d.txt";
        File file = new File(fileString);
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String strLine;
            while ((strLine = br.readLine()) != null) {
                String[] splitString = strLine.split(" ");
                //	System.out.println(strLine);
                //	System.out.println(splitString.length);
                if (splitString.length <= 300)
                    continue;
                for (int i = 1; i <= 300; i++) {
                    dataset[count][i - 1] = Double.valueOf(splitString[i]);
                }
                count++;
            }
        }
        dimension = 300;
        capacity = 30;
        indexNode rootBall = indexDSS.buildBalltree2(dataset, dimension, capacity, null, null, weight);
        FileUtil.write(edgeString, "datasetID,startNode,endNode,distance\n");
        FileUtil.write(nodeString, "datasetID,node,#CoverPoints,Depth,Radius,Leaf,PivotPoint\n");
        indexDSS.storeFeatures(rootBall, 1, 1, edgeString, nodeString, 1);
        // get the features of index tree,
        //	double features[] = datasetFeatures.getFeature(fetureString, rootBall,rootBall.getTotalCoveredPoints(), capacity, a);
        // featureMap.put(a, features);
    }

    // prepare two large scale datasets for pairwise comparison
    static Map<Integer, double[][]> prepareLargeDatasets(int numberofDatasets) {
        Map<Integer, double[][]> dataMapLargeMap = new HashMap<>();
        int count = 0;
        int length = 0, length1 = 0, length2 = 0;
        for (int id : dataMapPorto.keySet()) {
            double[][] dataset = dataMapPorto.get(id);
            length += dataset.length;
            count++;
            if (count == numberofDatasets) {
                length1 = length;
                length = 0;
            } else if (count >= 2 * numberofDatasets) {
                length2 = length;
                break;
            }
        }
        double[][] newdataset = new double[length1][dimension];
        double[][] newdataset1 = new double[length2][dimension];
        count = 0;
        length1 = 0;
        length2 = 0;
        for (int id : dataMapPorto.keySet()) {
            double[][] dataset = dataMapPorto.get(id);
            count++;
            if (count <= numberofDatasets) {
                for (int i = 0; i < dataset.length; i++) {
                    for (int j = 0; j < dimension; j++) {
                        newdataset[i + length1][j] = dataset[i][j];
                    }
                }
                length1 += dataset.length;
            } else if (count <= 2 * numberofDatasets && count > numberofDatasets) {
                for (int i = 0; i < dataset.length; i++) {
                    for (int j = 0; j < dimension; j++) {
                        newdataset1[i + length2][j] = dataset[i][j];
                    }
                }
                length2 += dataset.length;
            } else {
                break;
            }
        }
        dataMapLargeMap.put(1, newdataset);
        dataMapLargeMap.put(2, newdataset1);
        return dataMapLargeMap;
    }


    /*
     * this reads a single dataset after using the datalake index, instead of storing all the datasets in the main memory
     */
    public static double[][] readSingleFile(String datasetid) throws FileNotFoundException, IOException {

        File file = new File(aString + "/" + datasetid);
        long lineNumber = 0;
        try (Stream<String> lines = Files.lines(file.toPath())) {
            lineNumber = lines.count();
        }
        double[][] a = null;
        int i = 0;
        if (datalakeID == 4)
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {// reading argo
                String strLine;
                a = new double[(int) lineNumber - 1][];
                while ((strLine = br.readLine()) != null) {
                    String[] splitString = strLine.split(",");
                    String aString = splitString[6];
                    if (aString.matches("-?\\d+(\\.\\d+)?")) {
                        a[i] = new double[3];
                        a[i][0] = Double.valueOf(splitString[6]);
                        a[i][1] = Double.valueOf(splitString[7]);
                        a[i][2] = Double.valueOf(splitString[1].replace("-", ""));
                        i++;
                    }
                }
            }
        else if (datalakeID == 3)
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String strLine;
                a = new double[(int) lineNumber][];
                while ((strLine = br.readLine()) != null) {
                    String[] splitString = strLine.split(" ");
                    a[i] = new double[3];
                    a[i][0] = Double.valueOf(splitString[0]);
                    a[i][1] = Double.valueOf(splitString[1]);
                    a[i][2] = Double.valueOf(splitString[2]);
                    i++;
                }
            }
        else if (datalakeID == 5)
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String strLine;
                a = new double[(int) lineNumber][];
                while ((strLine = br.readLine()) != null) {
                    String[] splitString = strLine.split(",");
                    a[i] = new double[11];
                    a[i][0] = Double.valueOf(splitString[7]);
                    a[i][1] = Double.valueOf(splitString[8]);
                    a[i][2] = Double.valueOf(splitString[9]);
                    a[i][3] = Double.valueOf(splitString[10]);
                    a[i][4] = Double.valueOf(splitString[0]);
                    a[i][5] = Double.valueOf(splitString[1]);
                    a[i][6] = Double.valueOf(splitString[2]);
                    a[i][7] = Double.valueOf(splitString[3]);
                    a[i][8] = Double.valueOf(splitString[4]);
                    a[i][9] = Double.valueOf(splitString[5]);
                    a[i][10] = Double.valueOf(splitString[6]);
                    i++;
                }
            }
        return a;
    }

    /*
     * four methods, existing methods: R-tree, Pami, ball-tree, half-ball theory
     */
    static Pair<Double, PriorityQueue<queueMain>> testPairwiseHausdorff(int queryID, int datasetID, int testOrder, int timeID, boolean fastOnly) throws FileNotFoundException, IOException {
        // exact computation
        numberofComparisons = 4;
        int countAlgorithm = 0;
        double distance = 0;

        System.out.println("*******testing pairwise Hausdorff");
        // brute force, too slow, we ignore it here
        long startTime1 = System.nanoTime();
        //	double distance = Hausdorff.HausdorffExact(dataMapPorto.get(queryID), dataMapPorto.get(datasetID), dataMapPorto.get(datasetID)[0].length, false);
        long endtime = System.nanoTime();
        //	System.out.println((endtime-startTime1)/1000000000.0);
        //	System.out.println(distance);
        //	timeMultiple[0][testOrder*numberofComparisons+countAlgorithm++] = (endtime-startTime1)/1000000000.0;
        indexNode queryNode = null, datanode = null;
        Map<Integer, indexNode> queryindexmap = null, dataindexMap = null; // datasetIndex.get(queryid);
        double[][] querydata, dataset;
        if (dataMapPorto == null) {
            querydata = readSingleFile(datasetIdMapping.get(queryID));
            dataset = readSingleFile(datasetIdMapping.get(datasetID));
        } else {
            querydata = dataMapPorto.get(queryID);
            dataset = dataMapPorto.get(datasetID);
        }
        if (indexMap != null) {
            queryNode = indexMap.get(queryID);
            datanode = indexMap.get(datasetID);
        } else {
            if (datasetIndex != null) {
                queryindexmap = datasetIndex.get(queryID);
                dataindexMap = datasetIndex.get(datasetID);
                queryNode = queryindexmap.get(1);
                datanode = dataindexMap.get(1);
            } else {
                queryNode = indexDSS.buildBalltree2(querydata, dimension, capacity, null, null, weight);
                datanode = indexDSS.buildBalltree2(dataset, dimension, capacity, null, null, weight);
            }
        }

        // 1, using knn to search
        startTime1 = System.nanoTime();
        if (!fastOnly) {
            double pivot_distance = Util.EuclideanDis(queryNode.getPivot(), datanode.getPivot(), dimension);
            double ub = pivot_distance + queryNode.getRadius() + datanode.getRadius();
            distance = Hausdorff.HausdorffWithIndexDFS(querydata, dataset, dimension,
                    queryNode, datanode, 0, ub, indexDSS);
        }
        endtime = System.nanoTime();
        //	System.out.println(distance);
        timeMultiple[timeID][testOrder * numberofComparisons + countAlgorithm++] += (endtime - startTime1) / 1000000000.0;


        // 2, pvldb 11 using MBR bounds
        startTime1 = System.nanoTime();// exact search: 0, 1, 0
        AdvancedHausdorff.setBoundChoice(2); // setting the bound option
        if (!fastOnly) {
            AdvancedHausdorff.IncrementalDistance(querydata, dataset, dimension,
                    queryNode, datanode, 0, 1, 0, false, 0, false, queryindexmap, dataindexMap, null, true);
        }
        endtime = System.nanoTime();
        System.out.println((endtime - startTime1) / 1000000000.0 + "," + AdvancedHausdorff.disCompTime + ","
                + AdvancedHausdorff.EuclideanCounter);
        timeMultiple[timeID][testOrder * numberofComparisons + countAlgorithm++] += (endtime - startTime1) / 1000000000.0;

        // 3, pami 15
        startTime1 = System.nanoTime();// exact search: 0, 1, 0
        if (!fastOnly) {
            if (querydata.length < 10000)// will be out of memory if not
                distance = Hausdorff.earlyBreaking(querydata, dataset, dimension, false);
        }
        endtime = System.nanoTime();

        System.out.println((endtime - startTime1) / 1000000000.0);
        //	System.out.println(distance);
        timeMultiple[timeID][testOrder * numberofComparisons + countAlgorithm++] += (endtime - startTime1) / 1000000000.0;

        // 4, our fast algorithm
        AdvancedHausdorff.setBoundChoice(0);
        startTime1 = System.nanoTime();//exact search: 0, 1, 0
        Pair<Double, PriorityQueue<queueMain>> resultPair = AdvancedHausdorff.IncrementalDistance(querydata, dataset, dimension, queryNode, datanode, 0, 1, 0, false, 0, false, queryindexmap, dataindexMap, null, true);
        endtime = System.nanoTime();
        System.out.println((endtime - startTime1) / 1000000000.0 + "," + AdvancedHausdorff.disCompTime + "," + AdvancedHausdorff.EuclideanCounter);
        //	System.out.println(resultPair.getLeft());
        timeMultiple[timeID][testOrder * numberofComparisons + countAlgorithm++] += (endtime - startTime1) / 1000000000.0;
        return resultPair;

		/*
		 *
		// the basic version of our method
		startTime1 = System.nanoTime();
	//	distance = Hausdorff.IncrementalDistance(dataMapPorto.get(queryID), dataMapPorto.get(a), dimension, indexMap.get(1), indexMap.get(a), 1);
		distance = AdvancedHausdorff.IncrementalDistance(dataMapPorto.get(queryID), dataMapPorto.get(datasetID), dimension, indexMap.get(queryID), indexMap.get(datasetID), 0);
		endtime = System.nanoTime();
		System.out.println((endtime-startTime1)/1000000000.0);
		System.out.println(distance);
		timeMultiple[0][testOrder*numberofComparisons+countAlgorithm++] = (endtime-startTime1)/1000000000.0;


		startTime1 = System.nanoTime();//exact search: 0, 1, 0
		distance = AdvancedHausdorff.IncrementalDistanceDirected(dataMapPorto.get(queryID), dataMapPorto.get(datasetID), dimension, indexMap.get(queryID), indexMap.get(datasetID), 0, 1, 0, null, null, null, true);
		endtime = System.nanoTime();
		System.out.println((endtime-startTime1)/1000000000.0+ ","+ AdvancedHausdorff.disCompTime+","+AdvancedHausdorff.EuclideanCounter);
		System.out.println(distance);//+","+resultPair1.getLeft()+","+resultPair2.getLeft()
		System.out.println();
		timeMultiple[0][testOrder*numberofComparisons+countAlgorithm++] = (endtime-startTime1)/1000000000.0;
		*/
    }


    /*
     * we test the range query for union operation, just pairise
     */
    static void testPairwiseRangeQuery(indexNode dataseNode, double[][] dataset, int testOrder, int timeID) {
//        numberofComparisons = 4;
//
//        int countAlgorithm = 3;
//
//        double[] querymax = new double[dimension];
//        double[] querymin = new double[dimension];
//        for (int i = 0; i < dimension; i++) {// to enlarge the range for evaluation
//            querymax[i] = dataseNode.getPivot()[i] + error;
//            querymin[i] = dataseNode.getPivot()[i] - error;
//        }
//
//        // baseline method here, scan every datasets
//
//        long startTime1 = System.nanoTime();
//        double min_dis = Double.MAX_VALUE;
//        ArrayList<double[]> points = null;
//        dataseNode.coveredPOints(querymax, querymin, min_dis, dimension, dataset, null, dimNonSelected, dimensionAll, points);
//        long endtime = System.nanoTime();
//        System.out.println((endtime - startTime1) / 1000000000.0);
//        timeMultiple[timeID][testOrder * numberofComparisons + countAlgorithm++] += (endtime - startTime1) / 1000000000.0;
    }

    // restored index, not putting all the datasets and index in memeory, only access when it is checked after passing datalake index
    static void testRestoredIndex() {
//        /* test the restored index, it works well, while it is a little slower because of finding the hashmap */
//        long startTime1 = System.nanoTime();//exact search: 0, 1, 0
//        Pair<Double, PriorityQueue<queueMain>> resultPairq = AdvancedHausdorff.IncrementalDistance(dataMapPorto.get(1), dataMapPorto.get(3), dimension, indexMap.get(1), indexMap.get(3), 0, 1, 0, false, 0, false, null, null, null, true);
//        long endtime = System.nanoTime();
//        System.out.println((endtime - startTime1) / 1000000000.0 + "," + AdvancedHausdorff.disCompTime + "," + AdvancedHausdorff.EuclideanCounter);
//        System.out.println(resultPairq.getLeft());
//
//        startTime1 = System.nanoTime();//exact search: 0, 1, 0
//        datasetIndex = indexDSS.restoreIndex(indexString, dimension, dataMapPorto);
//        endtime = System.nanoTime();
//        //	System.out.println("building index costs: "+(endtimea-startTime1a)/1000000000.0);
//        System.out.println("reconstruction costs: " + (endtime - startTime1) / 1000000000.0);
//
//        startTime1 = System.nanoTime();//exact search: 0, 1, 0
//        Pair<Double, PriorityQueue<queueMain>> resultPair11 = AdvancedHausdorff.IncrementalDistance(dataMapPorto.get(1), dataMapPorto.get(3), dimension, datasetIndex.get(1).get(1), datasetIndex.get(3).get(1), 0, 1, 0, false, 0, false, datasetIndex.get(1), datasetIndex.get(3), null, true);
//        endtime = System.nanoTime();
//        System.out.println((endtime - startTime1) / 1000000000.0 + "," + AdvancedHausdorff.disCompTime + "," + AdvancedHausdorff.EuclideanCounter);
//        System.out.println(resultPair11.getLeft());
    }

    static indexNode getQueryNode(double[][] querydata, int queryid) throws FileNotFoundException, IOException {
//        indexNode queryNode = null;
//        if (dataMapPorto == null)
//            querydata = readSingleFile(datasetIdMapping.get(queryid));
//        else
//            querydata = dataMapPorto.get(queryid);
//        if (indexMap != null) {
//            System.out.println();
//            queryNode = indexMap.get(queryid);
//        } else {
//            if (datasetIndex != null) {
//                queryNode = datasetIndex.get(queryid).get(1);
//            } else {
//                queryNode = indexDSS.buildBalltree2(querydata, dimension, capacity, null, null, weight);
//            }
//        }
//        return queryNode;
        return null;
    }

    /**
     * four methods: scanning-all, sigspatial10, early adandoning, datalake
     */
    static void testTopkHausdorff(int k, int testOrder, int queryid, int timeID, int limit) throws FileNotFoundException, IOException {
//        numberofComparisons = 4;
//        int countAlgorithm = 0;
//        /* read query dataset */
//        indexNode queryNode = null;
//        Map<Integer, indexNode> queryindexmap = null; // datasetIndex.get(queryid);
//        double[][] querydata;
//        if (dataMapPorto == null)
//            querydata = readSingleFile(datasetIdMapping.get(queryid));
//        else
//            querydata = dataMapPorto.get(queryid);
//        if (indexMap != null) {
//            queryNode = indexMap.get(queryid);
//        } else {
//            if (datasetIndex != null) {
//                queryindexmap = datasetIndex.get(queryid);
//                queryNode = queryindexmap.get(1);
//            } else {
//                queryNode = indexDSS.buildBalltree2(querydata, dimension, capacity, null, null, weight);
//            }
//        }
//
//        System.out.println("*******testing top-k Hausdorff");
//        AdvancedHausdorff.setBoundChoice(0);//using our fast ball bound
//
//        // 1, conduct top-k dataset search, the baseline, scan one by one
//        long startTime1 = System.nanoTime();// exact search: 0, 1, 0
//        //Search.hausdorffEarylyAbandonging(dataMapPorto, queryid, indexMap, dimension, limit, false, null, null, null, true);//no top-k early breaking
//        long endtime = System.nanoTime();
//        System.out.println("top-k search costs: " + (endtime - startTime1) / 1000000000.0);
//        timeMultiple[timeID][testOrder * numberofComparisons + countAlgorithm++] += (endtime - startTime1) / 1000000000.0;
//
//
//        // 2, early abandoning, compute the bound first, then see whether to abandon it
//        startTime1 = System.nanoTime();// exact search: 0, 1, 0
//        Search.hausdorffEarylyAbandonging(dataMapPorto, queryid, indexMap, dimension, limit, true, saveDatasetIndex,
//                dimNonSelected, dimensionAll, querydata, datasetIdMapping, capacity, weight, queryindexmap, queryNode, indexString);
//        endtime = System.nanoTime();
//        System.out.println("top-k search costs: " + (endtime - startTime1) / 1000000000.0);
//        timeMultiple[timeID][testOrder * numberofComparisons + countAlgorithm++] += (endtime - startTime1) / 1000000000.0;
//
//        // 3, rank all the candidate by lower bounds, then one by one
//        startTime1 = System.nanoTime();// exact search: 0, 1, 0
//        int datasetID = Search.HausdorffEarylyAbandongingRanking(dataMapPorto, queryid, indexMap, dimension, limit, resultPair, queryindexmap, saveDatasetIndex, dimNonSelected, dimensionAll, error, querydata, datasetIdMapping, queryNode, capacity, weight, indexString);
//        endtime = System.nanoTime();
//        System.out.println("top-k search costs: " + (endtime - startTime1) / 1000000000.0);
//        timeMultiple[timeID][testOrder * numberofComparisons + countAlgorithm++] += (endtime - startTime1) / 1000000000.0;
//
//        // 4, using datalake index to prune
//        startTime1 = System.nanoTime();// exact search: 0, 1, 0
//        HashMap<Integer, Double> result = Search.pruneByIndex(dataMapPorto, datasetRoot, queryNode, queryid,
//                dimension, indexMap, datasetIndex, queryindexmap, datalakeIndex, datasetIdMapping, k,
//                indexString, dimNonSelected, dimensionAll, error, capacity, weight, saveDatasetIndex, querydata);
//        endtime = System.nanoTime();
//
//        //TODO haus test some of the result matrix is remote to the query dataset
//        List<double[][]> list = result.entrySet().stream().map(entry -> getMatrixByDatasetId(entry.getKey())).collect(Collectors.toList());
//
//        for (Map.Entry e : result.entrySet()) {
//            System.out.println(e.getKey());
//        }
//        System.out.println("top-k search costs: " + (endtime - startTime1) / 1000000000.0);
//        System.out.println();
//        timeMultiple[timeID][testOrder * numberofComparisons + countAlgorithm++] += (endtime - startTime1) / 1000000000.0;
    }


    /*
     * test the approximate version of knn and pairwise by increasing the error,
     * we have two comparisons, pairwise and Hausdorff
     */
    static void testHausdorffApproxi(int k, int testOrder, int queryid, int timeID) throws FileNotFoundException, IOException {
//        numberofComparisons = 4;
//        int countAlgorithm = 2;
//
//        System.out.println("*******testing approximate Hausdorff");
//
//        // 1, pairwise
//        int datasetID = queryid + 1;
//        long startTime1 = System.nanoTime();
//        double dataMap[][], query[][];
//        if (dataMapPorto != null) {
//            dataMap = dataMapPorto.get(datasetID);
//            query = dataMapPorto.get(queryid);
//        } else {
//            dataMap = Framework.readSingleFile(datasetIdMapping.get(datasetID));
//            query = Framework.readSingleFile(datasetIdMapping.get(queryid));
//        }
//        indexNode queryNode, datanode;
//        Map<Integer, indexNode> queryindexmap = null, dataindexMap = null;
//        if (indexMap != null) {
//            queryNode = indexMap.get(queryid);
//            datanode = indexMap.get(datasetID);
//        } else {
//            if (datasetIndex != null) {
//                queryindexmap = datasetIndex.get(queryid);
//                dataindexMap = datasetIndex.get(datasetID);
//                queryNode = queryindexmap.get(1);
//                datanode = dataindexMap.get(1);
//            } else {
//                queryNode = indexDSS.buildBalltree2(query, dimension, capacity, null, null, weight);
//                datanode = indexDSS.buildBalltree2(dataMap, dimension, capacity, null, null, weight);
//            }
//        }
//
//        Pair<Double, PriorityQueue<queueMain>> resultPair = AdvancedHausdorff.IncrementalDistance(query, dataMap, dimension, queryNode, datanode, 0, 1, 0, false, 0, false, queryindexmap, dataindexMap, null, true);
//        long endtime = System.nanoTime();
//        System.out.println((endtime - startTime1) / 1000000000.0 + "," + AdvancedHausdorff.disCompTime + "," + AdvancedHausdorff.EuclideanCounter);
//        //	System.out.println(resultPair.getLeft());
//        timeMultiple[0][testOrder * numberofComparisons + countAlgorithm++] += (endtime - startTime1) / 1000000000.0;
//
//        // 2, top-k fast
//        startTime1 = System.nanoTime();// exact search: 0, 1, 0
//        HashMap<Integer, Double> result = Search.pruneByIndex(dataMapPorto, datasetRoot, queryNode, queryid,
//                dimension, indexMap, datasetIndex, queryindexmap, datalakeIndex, datasetIdMapping, k,
//                indexString, null, true, error, capacity, weight, saveDatasetIndex, query);
//        System.out.println("appro----");
//        for (Map.Entry e : result.entrySet()) {
//            System.out.println(e.getKey());
//        }
//        endtime = System.nanoTime();
//        timeMultiple[timeID][testOrder * numberofComparisons + countAlgorithm++] += (endtime - startTime1) / 1000000000.0;
    }

    /*
     * join and self-join
     */
    static void testJoinOutlier(int testOrder, int timeID, int queryID, int datasetID, Pair<Double, PriorityQueue<queueMain>> aPair) {
//        numberofComparisons = 4;
//        int countAlgorithm = 0;
//		/*
//		System.out.println("*******testing join and outlier");
//
//		// 1. brute force, too slow
//		long startTime1 = System.nanoTime();
//	//	Join.scanning(dataMapPorto.get(queryID), dataMapPorto.get(datasetID), dimension);
//		long endtime = System.nanoTime();
//		System.out.println("join with top-1 dataset with scanning baseline costs: "+(endtime-startTime1)/1000000000.0);
//		timeMultiple[timeID][testOrder*numberofComparisons+countAlgorithm++] += (endtime-startTime1)/1000000000.0;
//
//		// 2, using knn
//		startTime1 = System.nanoTime();// exact search: 0, 1, 0
//		Join.joinTableBaseline(dataMapPorto.get(queryID), dataMapPorto.get(datasetID),
//				indexMap.get(queryID), indexMap.get(datasetID), dimension, indexDSS);
//		endtime = System.nanoTime();
//		timeMultiple[timeID][testOrder*numberofComparisons+countAlgorithm++] += (endtime-startTime1)/1000000000.0;
//		System.out.println("join with top-1 dataset with nn search baseline costs: "+(endtime-startTime1)/1000000000.0);*/
//
//        // 3, join with our cached queue
//        //startTime1 = System.nanoTime();// exact search: 0, 1, 0
//        Join.IncrementalJoin(dataMapPorto.get(queryID), dataMapPorto.get(datasetID), dimension, indexMap.get(limit + 1),
//                indexMap.get(datasetID), 0, 0, 0.01, false, 0, false, aPair.getLeft(), aPair.getRight(), null, null, "haus", null, true);
//        //endtime = System.nanoTime();
//        //timeMultiple[timeID][testOrder*numberofComparisons+countAlgorithm++] += (endtime-startTime1)/1000000000.0;
//        //System.out.println("join with top-1 dataset on reused queues costs: "+(endtime-startTime1)/1000000000.0);
//
//		/*
//		// 4, test self-join for outlier detection
//		startTime1 = System.nanoTime();// exact search: 0, 1, 0
//		AdvancedHausdorff.setParameter(true, false);
//		Pair<Double, PriorityQueue<queueMain>> resultPaira = AdvancedHausdorff.IncrementalDistance(dataMapPorto.get(1), dataMapPorto.get(1), dimension, indexMap.get(1), indexMap.get(1), 0, 1, 0, false, 0, false, null, null, null, true);
//		endtime = System.nanoTime();
//		System.out.println((endtime-startTime1)/1000000000.0+ ","+ AdvancedHausdorff.disCompTime+","+AdvancedHausdorff.EuclideanCounter);
//		timeMultiple[timeID][testOrder*numberofComparisons+countAlgorithm++] += (endtime-startTime1)/1000000000.0;*/
//        //	System.out.println(resultPaira.getLeft());
//        //	System.out.println();
    }


    /**
     * four methods: intersecting range, overlapped grids in scanning and pruning mode
     */
    static void testRangeQuery(int k, int testOrder, int queryid, int timeID) throws FileNotFoundException, IOException {
//        //scan every dataset to find the top-k
//        numberofComparisons = 4;
//        int countAlgorithm = 0;
//        indexNode root = datasetRoot;//datalakeIndex.get(1);//use storee
//        if (datalakeIndex != null)
//            root = datalakeIndex.get(1);
//
//        System.out.println("*******testing top-k range query");
//
//        double[] querymax = new double[dimension];
//        double[] querymin = new double[dimension];
//        double[][] dataset = null;
//        indexNode queryNode = getQueryNode(dataset, queryid);
//        for (int i = 0; i < dimension; i++) {// to enlarge the range for evaluation
//            querymax[i] = queryNode.getMBRmax()[i] + error;
//            querymin[i] = queryNode.getMBRmin()[i] - error;
//        }
//
//        // 1, the query by intersecting area, scan every dataset
//        Search.setScanning(true);
//        HashMap<Integer, Double> result = new HashMap<>();
//        long startTime1 = System.nanoTime();
//        Search.rangeQueryRankingArea(root, result, querymax, querymin, Double.MAX_VALUE, k, null, dimension,
//                datalakeIndex, dimNonSelected, dimensionAll);
//        long endtime = System.nanoTime();
//        System.out.println((endtime - startTime1) / 1000000000.0);
//        timeMultiple[timeID][testOrder * numberofComparisons + countAlgorithm++] += (endtime - startTime1) / 1000000000.0;
//
//        // 2, the grid-based overlap
//        int[] queryzcurve = new int[zcodemap.get(queryid).size()];
//        startTime1 = System.nanoTime();
//        for (int i = 0; i < zcodemap.get(queryid).size(); i++)
//            queryzcurve[i] = zcodemap.get(queryid).get(i);
//        HashMap<Integer, Double> result2 = EffectivenessStudy.topkAlgorithmZcurveHashMap(zcodemap, zcodemap.get(queryid), k);
//        endtime = System.nanoTime();
//        System.out.println((endtime - startTime1) / 1000000000.0);
//        timeMultiple[timeID][testOrder * numberofComparisons + countAlgorithm++] += (endtime - startTime1) / 1000000000.0;
//
//        // 3, use datalake index to rank by area
//        startTime1 = System.nanoTime();
//        Search.setScanning(false);
//        result = new HashMap<>();
//        Search.rangeQueryRankingArea(root, result, querymax,
//                querymin, Double.MAX_VALUE, k, null, dimension, datalakeIndex, dimNonSelected, dimensionAll);
//        endtime = System.nanoTime();
//        System.out.println((endtime - startTime1) / 1000000000.0);
//        timeMultiple[timeID][testOrder * numberofComparisons + countAlgorithm++] += (endtime - startTime1) / 1000000000.0;
//
//
//        // 4, use datalake index to rank by number of overlapped grids
//        startTime1 = System.nanoTime();
//        result = new HashMap<>();
//        Search.gridOverlap(root, result, queryzcurve, Double.MAX_VALUE, k, null, datalakeIndex);
//        endtime = System.nanoTime();
//        System.out.println((endtime - startTime1) / 1000000000.0);
//        timeMultiple[timeID][testOrder * numberofComparisons + countAlgorithm++] += (endtime - startTime1) / 1000000000.0;
    }

    /*
     * we only test the varied resolution here
     */
    static void testGridOverlapAlone(int k, int testOrder, int queryid, int timeID) {
//        numberofComparisons = 4;
//        int countAlgorithm = 3;
//
//        System.out.println("*******testing top-k grip overlap");
//
//        int[] queryzcurve = new int[zcodemap.get(queryid).size()];
//        long startTime1 = System.nanoTime();
//        for (int i = 0; i < zcodemap.get(queryid).size(); i++)
//            queryzcurve[i] = zcodemap.get(queryid).get(i);
//        EffectivenessStudy.topkAlgorithmZcurveHashMap(zcodemap, zcodemap.get(queryid), k);
//        long endtime = System.nanoTime();
//        System.out.println((endtime - startTime1) / 1000000000.0);
//        timeMultiple[timeID][testOrder * numberofComparisons + countAlgorithm++] += (endtime - startTime1) / 1000000000.0;
    }

    /**
     * range query by using dataset index, i.e., count how many points in the range, and plus return all the points
     */

    static void testRangeQueryDeep(int k, int testOrder, int queryid, int timeID) throws FileNotFoundException, IOException {
//        //scan every dataset to find the top-k
//        numberofComparisons = 4;
//        int countAlgorithm = 2;
//        long startTime1 = System.nanoTime();
//
//        System.out.println("*******testing top-k range query by covered points");
//
//        double[] querymax = new double[dimension];
//        double[] querymin = new double[dimension];
//        for (int i = 0; i < dimension; i++) {// enlarge the range for evaluation
//            querymax[i] = indexMap.get(queryid).getMBRmax()[i] + error;
//            querymin[i] = indexMap.get(queryid).getMBRmin()[i] - error;
//        }
//
//        indexNode root = datasetRoot;//datalakeIndex.get(1);//use storee
//        if (datalakeIndex != null)
//            root = datalakeIndex.get(1);
//
//        // 1, counting how many points in the range, access the dataset index
//        ArrayList<double[]> points = new ArrayList<>();
//        // find and store the founded points
//        Search.setScanning(true);
//        HashMap<Integer, Double> result = new HashMap<>();
//        startTime1 = System.nanoTime();// exact search: 0, 1, 0
//        Search.rangeQueryRankingNumberPoints(root, result, querymax,
//                querymin, Double.MAX_VALUE, k, null, dimension, dataMapPorto, datalakeIndex,
//                datasetIndex, datasetIdMapping, indexString, indexDSS, dimNonSelected, dimensionAll, points, indexMap, capacity, weight, saveDatasetIndex);
//        long endtime = System.nanoTime();
//        System.out.println((endtime - startTime1) / 1000000000.0);
//        timeMultiple[timeID][testOrder * numberofComparisons + countAlgorithm++] += (endtime - startTime1) / 1000000000.0;
//
//        // 2, stored the points in the range, access the dataset index
//        points = null;
//        startTime1 = System.nanoTime();
//        Search.setScanning(false);
//        result = new HashMap<>();
//        startTime1 = System.nanoTime();// exact search: 0, 1, 0
//        Search.rangeQueryRankingNumberPoints(root, result, querymax,
//                querymin, Double.MAX_VALUE, k, null, dimension, dataMapPorto, datalakeIndex,
//                datasetIndex, datasetIdMapping, indexString, indexDSS, dimNonSelected, dimensionAll, points, indexMap, capacity, weight, saveDatasetIndex);
//        endtime = System.nanoTime();
//        System.out.println((endtime - startTime1) / 1000000000.0);
//        timeMultiple[timeID][testOrder * numberofComparisons + countAlgorithm++] += (endtime - startTime1) / 1000000000.0;
    }

    /*
     * running time record
     */
    static void recordRunningTime(String filename, int timeid) {
//        String content = datalakeID + ",";
//        for (double t : timeMultiple[timeid])
//            content += t / NumberQueryDataset + ",";
//        write(filename, content + "\n");
    }

    /*
     * running time record of
     */
    static void recordIndexModeEffect(String filename1, String filename2) {
//        String content = datalakeID + ",";
//        for (int i = 0; i < numberofParameterChoices; i++) {
//            content += "0,";
//            for (int j = 0; j < 3; j++) {
//                double t = timeMultiple[j * 2][i * numberofComparisons + 3];
//                content += t / NumberQueryDataset + ",";
//            }
//        }
//        write(filename1, content + "\n");
//
//        for (int i = 0; i < numberofParameterChoices; i++) {
//            content += "0,";
//            for (int j = 0; j < 3; j++) {
//                double t = timeMultiple[j * 2 + 1][i * numberofComparisons + 3];
//                content += t / NumberQueryDataset + ",";
//            }
//        }
//        write(filename2, content + "\n");
    }

    /*
     * running time record
     */
    static void recordIndexPerformance(String filename, int timeid, double spaceMultiple[][]) {
//        String content = datalakeID + ",";
//        for (double t : spaceMultiple[timeid])
//            content += t / NumberQueryDataset + ",";
//        write(filename, content + "\n");
    }

    /*
     * update the datalake
     */
    static void addingDatasets() throws FileNotFoundException, IOException {
//        /**
//         * test adding new dataset into the datalake
//         */
////		indexNodes = new ArrayList<indexNode>();
////		for(int dataid: datasetIndex.keySet()) {
////			indexNodes.add(datasetIndex.get(dataid).get(1));
////		}
////		indexDSS.updateDatasetNodeId(datasetRoot, 1);
////		datalakeIndex = null;
////		/* update index */
////		indexNode rootBall = indexDSS.buildBalltree2(dataMapPorto.get(limit+1), dimension, capacity);
////		rootBall.setroot(limit+1);
////		datasetRoot = indexDSS.insertNewDataset(rootBall, datasetRoot, capacity, dimension);
////		int rootNum = indexDSS.getRootCount(datasetRoot);
////		System.out.println("number of datasets: "+ rootNum);
////		dataMapPorto = null;
//
//
//        int k = 10;
//        long startTime1 = System.nanoTime();
//        // test top-k search, which also works for restored index.
//        int queryid = 10;
//        double[][] querydata = readSingleFile(datasetIdMapping.get(queryid));
//        HashMap<Integer, Double> result = Search.pruneByIndex(dataMapPorto, datasetRoot, indexMap.get(queryid), queryid,
//                dimension, indexMap, datasetIndex, datasetIndex.get(queryid), datalakeIndex, datasetIdMapping, k,
//                indexString, null, true, error, capacity, weight, saveDatasetIndex, querydata);
//        long endtime = System.nanoTime();
//        System.out.println("top-1 search costs: " + (endtime - startTime1) / 1000000000.0);
//        System.out.println(result + ", " + datasetIdMapping.get(result.entrySet().iterator().next().getKey()));
    }


    /*
     * pair-wise comparison, including Hausdorff, join, outlier, by varying the dataset scale, and recreate the index
     */
    static void EvaluatePairWise() throws IOException {
//        System.out.println("evaluate pair-wise hausdorff");
//        timeMultiple = new double[3][numberofComparisons * numberofParameterChoices];
//        for (int i = 0; i < numberofParameterChoices; i++) { // test the scale
//            for (int j = 0; j < NumberQueryDataset; j++) {
//                int scale = ss[i];
//                limit = 2 * scale;
////				storeIndexMemory = false;
////				readDatalake(limit);
////				storeIndexMemory = true;
////				indexNodes = new ArrayList<>();
//                //dataMapPorto = prepareLargeDatasets(scale); // generate datasets by combining more 10, 100, 1000, 10000 datasets
////				long startTime1a = System.nanoTime();
////				for(int a:dataMapPorto.keySet()) {//we can build index to accelerate
////					if(a>limit+NumberQueryDataset)
////						break;
////					createDatasetIndex(a, dataMapPorto.get(a)); // load index structure
////				}
////				long endtimea = System.nanoTime();
////				write("./logs/spadas/efficiency/DatasetPairwiseIndexTime.txt", datalakeID+","+limit+","+dimension+","+capacity+","+(endtimea-startTime1a)/1000000000.0+"\n");
////				write("./logs/spadas/efficiency/DatasetPairwiseIndexSize.txt", datalakeID+","+limit+","+dimension+","+capacity+","+(getDatalakeIndexSize(datasetRoot, false)+getAllDatasetIndexSize())/(1024.0*1024)+"\n");
//
//                // 1, the effect of dataset scale on pairwise
//                Pair<Double, PriorityQueue<queueMain>> resultPair = testPairwiseHausdorff(1, 2, i, 0, false);// 1,
//
//                // 2, the effect of dataset scale on joins
//                testJoinOutlier(i, 1, 2, 200, resultPair); // 2,
//
//                // 3, the effect of scale on range query
//				/*error = spaceRange/((int) Math.pow(2, rs[0]));
//				testPairwiseRangeQuery(indexMap.get(1), dataMapPorto.get(1), i, 2);*/
//            }
//        }
//        recordRunningTime("./logs/spadas/efficiency/" + datalakeID + "-n-haus.txt", 0);
//        recordRunningTime("./logs/spadas/efficiency/" + datalakeID + "-n-join-outlier.txt", 1);
//        recordRunningTime("./logs/spadas/efficiency/" + datalakeID + "-n-range-deep.txt", 1);
//
//        System.out.println("evaluate pair-wise range");
//        timeMultiple = new double[1][numberofComparisons * numberofParameterChoices];
//        for (int i = 0; i < numberofParameterChoices; i++) { // test range
//            for (int j = 0; j < NumberQueryDataset; j++) {
//                resolution = rs[i];
//                error = spaceRange / ((int) Math.pow(2, resolution));
//                testPairwiseRangeQuery(indexMap.get(1), dataMapPorto.get(1), i, 0);
//            }
//        }
//        recordRunningTime("./logs/spadas/efficiency/" + datalakeID + "-r-range-deep.txt", 0);
    }

    static void EvaluateHausCapacity() throws IOException {
//        System.out.println("evaluate capacity's effect on hausdorff");
//        timeMultiple = new double[1][numberofComparisons * numberofParameterChoices];
//        for (int i = 0; i < numberofParameterChoices; i++) { // test the scale
//            for (int j = 0; j < NumberQueryDataset; j++) {
//                int scale = 100;
//                if (datalakeID == 3)
//                    scale = 1;
//                limit = 2 * scale; // limit is changed here
//                storeIndexMemory = false;
//                readDatalake(limit);
//                storeIndexMemory = true;
//                indexNodes = new ArrayList<>();
//                dataMapPorto = prepareLargeDatasets(scale); // generate datasets by combining more 10, 100, 1000, 10000 datasets
//                capacity = fs[i];
//                for (int a : dataMapPorto.keySet()) {//we can build index to accelerate
//                    if (a > limit + NumberQueryDataset)
//                        break;
//                    createDatasetIndex(a, dataMapPorto.get(a)); // load index structure
//                }
//                testPairwiseHausdorff(1, 2, i, 0, true);// test fast only
//            }
//        }
//        recordRunningTime("./logs/spadas/efficiency/" + datalakeID + "-f-haus.txt", 0);
    }

    private static void EvaluateDatalakeScale() throws FileNotFoundException, IOException {

//        timeMultiple = new double[2][numberofComparisons * numberofParameterChoices];
//        indexNodes = new ArrayList<>();
//        System.out.println("evaluate datalake's scale");
//        for (int Ni = 0; Ni < ns.length; Ni++) {// varying datalake scale, preparing datalake index, and evaluate top-k search
//            int N = (int) (ns[Ni] * limit);
//            int a = 1;
//            for (indexNode node : indexNodesAll) {//
//                if (a++ <= N) // we only index part of the dataset.
//                    indexNodes.add(node);
//            }
//
//            long startTime1a = System.nanoTime();
//            File tempFile = new File(indexString + "datalake" + N + "-" + capacity + "-" + dimension + ".txt");
//            if (!tempFile.exists()) {
//                storeIndexMemory = true;
//            } else {
//                storeIndexMemory = false;
//            }
//            createDatalake(N);// create or load the index of whole data lake, and store it into disk to avoid rebuilding it
//            long endtimea = System.nanoTime();
//            write("./logs/spadas/efficiency/DatalakeIndexTime.txt", datalakeID + "," + N + "," + dimension + "," + capacity + "," + (endtimea - startTime1a) / 1000000000.0 + "\n");
//            write("./logs/spadas/efficiency/DatasetIndexSize.txt", datalakeID + "," + limit + "," + dimension + "," + capacity + "," + (getDatalakeIndexSize(datasetRoot, false) + getAllDatasetIndexSize()) / (1024.0 * 1024) + "\n");
//            for (int j = 0; j < NumberQueryDataset; j++) {
//                testTopkHausdorff(10, Ni, j + limit, 0, limit); // 6,
//                testRangeQuery(10, Ni, j + limit, 1); // 7,
//            }
//        }
//        recordRunningTime("./logs/spadas/efficiency/" + datalakeID + "-N-topk-haus.txt", 0);
//        recordRunningTime("./logs/spadas/efficiency/" + datalakeID + "-N-range.txt", 1);
    }

    /*
     * evaluate the effect of f on the datalake index
     */
    static void EvaluateDatalakeCapacity() throws IOException {
//        timeMultiple = new double[2][numberofComparisons * numberofParameterChoices];
//        indexNodes = new ArrayList<>();
//        int N = limit;
//        int a = 1;
//        for (indexNode node : indexNodesAll) {//
//            if (a++ <= N) // we only index part of the dataset.
//                indexNodes.add(node);
//        }
//        System.out.println("evaluate datalake index's capacity");
//        for (int i = 0; i < numberofParameterChoices; i++) { // test the scale
//            capacity = fs[i];
//            long startTime1a = System.nanoTime();
//            File tempFile = new File(indexString + "datalake" + N + "-" + capacity + "-" + dimension + ".txt");
//            if (!tempFile.exists()) {
//                storeIndexMemory = true;
//            } else {
//                storeIndexMemory = false;
//            }
//            createDatalake(N);// create or load the index of whole data lake, and store it into disk to avoid
//            long endtimea = System.nanoTime();
//            write("./logs/spadas/efficiency/DatalakeIndexTime.txt", datalakeID + "," + N + "," + dimension + "," + capacity + "," + (endtimea - startTime1a) / 1000000000.0 + "\n");
//            write("./logs/spadas/efficiency/DatasetIndexSize.txt", datalakeID + "," + limit + "," + dimension + "," + capacity + "," + (getDatalakeIndexSize(datasetRoot, false) + getAllDatasetIndexSize()) / (1024.0 * 1024) + "\n");
//
//            for (int j = 0; j < NumberQueryDataset; j++) {
//                testTopkHausdorff(10, i, j + limit, 0, limit); // 6, test the top-k Hausdorff
//                testRangeQuery(10, i, j + limit, 1); // 7, test the range query
//            }
//        }
//        recordRunningTime("./logs/spadas/efficiency/" + datalakeID + "-f-topk.txt", 0);
//        recordRunningTime("./logs/spadas/efficiency/" + datalakeID + "-f-range.txt", 1);
    }

    /*
     * evaluate the effect of dimension on pairwise Hausdorff
     */
    static void EvaluateHausDimension() throws IOException {
//        System.out.println("evaluate dimenison's effect on hausdorff");
//        if (datalakeID == 5) {
//            timeMultiple = new double[1][numberofComparisons * numberofParameterChoices];
//            for (int i = 0; i < numberofParameterChoices; i++) { // test the dimension
//                for (int j = 0; j < NumberQueryDataset; j++) {
//                    dimension = ds[i];
//                    dimensionAll = false;
//                    dimNonSelected = new boolean[11];
//                    for (int p = dimension; p < 11; p++)
//                        dimNonSelected[p] = true;// non selected
//                    testPairwiseHausdorff(1, 2, i, 0, false);// test all
//                }
//            }
//            recordRunningTime("./logs/spadas/efficiency/" + datalakeID + "-d-haus.txt", 0);
//        }
    }

    /*
     * evaluate top-k range query which only use datalake
     * 4 comparisons, ranking by
     * intersecting area, and grid-based overlap
     */
    static void EvaluateRangequery() throws FileNotFoundException, IOException {
//        // range query by datalake index only
//        System.out.println("evaluate range query");
//        timeMultiple = new double[2][numberofComparisons * numberofParameterChoices];
//        for (int i = 0; i < numberofParameterChoices; i++) { // test k
//            for (int j = 0; j < NumberQueryDataset; j++) {
//                // 1, the
//                //TODO change 3rd param from j+limit to j+1
//                testRangeQuery(ks[i], i, j + 1, 0);
//                //	testRangeQueryDeep(ks[i], i, j + limit, 1);
//            }
//        }
//        recordRunningTime("./logs/spadas/efficiency/" + datalakeID + "-k-range.txt", 0);
//        //	recordRunningTime("./logs/spadas/efficiency/"+datalakeID+"-k-range-deep.txt", 1);
//
//        // range query by datalake index only
////		timeMultiple = new double[2][numberofComparisons * numberofParameterChoices];
////		for (int i = 0; i < numberofParameterChoices; i++) { // test range
////			for (int j = 0; j < NumberQueryDataset; j++) {
////				resolution = rs[i];
////				error = spaceRange/((int) Math.pow(2, resolution));
////				//TODO change 3rd param from j+limit to j+1
////				testRangeQuery(10, i, j + 1, 0);
////	//			testRangeQueryDeep(10, i, j + limit, 1);
////			}
////		}
////		recordRunningTime("./logs/spadas/efficiency/"+datalakeID+"-r-range.txt", 0);
//        //	recordRunningTime("./logs/spadas/efficiency/"+datalakeID+"-r-range-deep.txt", 1);
//
//        if (datalakeID == 5) {
//            timeMultiple = new double[2][numberofComparisons * numberofParameterChoices];
//            for (int i = 0; i < numberofParameterChoices; i++) { // test only the range query, as z-curve does not work
//                for (int j = 0; j < NumberQueryDataset; j++) {
//                    dimension = ds[i];
//                    dimensionAll = false;
//                    dimNonSelected = new boolean[11];
//                    for (int p = dimension; p < 11; p++)
//                        dimNonSelected[p] = true;// non selected
//                    testRangeQuery(10, i, j + limit, 0);
//                    //				testRangeQueryDeep(10, i, j + limit, 1);
//                }
//            }
//            recordRunningTime("./logs/spadas/efficiency/" + datalakeID + "-d-range.txt", 0);
//            //		recordRunningTime("./logs/spadas/efficiency/"+datalakeID+"-d-range-deep.txt", 1);
//            dimension = 11;
//        }
    }

    /*
     * top-k hausdorff query by datasets
     */
    static void EvaluateTopkHaus() throws FileNotFoundException, IOException {
//        System.out.println("evaluate top-k Haus");
//
//        timeMultiple = new double[1][numberofComparisons * numberofParameterChoices];
//        for (int i = 0; i < ks.length; i++) { // varying k
//            for (int j = 0; j < NumberQueryDataset; j++)
//                //TODO change 3rd param from j+limit to j+1
//                testTopkHausdorff(ks[i], i, 34, 0, limit);
//        }
//        recordRunningTime("./logs/spadas/efficiency/" + datalakeID + "-k-topk-haus.txt", 0);
//
//        if (datalakeID == 5) {
//            timeMultiple = new double[1][numberofComparisons * numberofParameterChoices];
//            for (int i = 0; i < ds.length; i++) { // varying d
//                for (int j = 0; j < NumberQueryDataset; j++) {
//                    dimension = ds[i];
//                    dimensionAll = false;
//                    dimNonSelected = new boolean[11];
//                    for (int p = dimension; p < 11; p++)
//                        dimNonSelected[p] = true;// non selected
//                    testTopkHausdorff(10, i, j + limit, 0, limit);
//                }
//            }
//            recordRunningTime("./logs/spadas/efficiency/" + datalakeID + "-d-topk-haus.txt", 0);
//        }
//
//        timeMultiple = new double[1][numberofComparisons * numberofParameterChoices];
//        for (int i = 0; i < numberofParameterChoices; i++) { // test range
//            for (int j = 0; j < NumberQueryDataset; j++) {
//                resolution = rs[i];
//                error = spaceRange / ((int) Math.pow(2, resolution));
//                //TODO change 3rd param from j+limit to j+1
//                testHausdorffApproxi(10, i, 34, 0);//cannot work for porto
//            }
//        }
//        recordRunningTime("./logs/spadas/efficiency/" + datalakeID + "-r-haus-appro.txt", 0);
    }

    private static void EvaluateGridResolution() throws FileNotFoundException, IOException {
//        System.out.println("evaluate grid resolution on GBO query");
//        dimension = 2;
//        timeMultiple = new double[1][numberofComparisons * numberofParameterChoices];
//        for (int Ri = 0; Ri < rs.length; Ri++) {
//            resolution = rs[Ri];
//            error = spaceRange / ((int) Math.pow(2, resolution));
//            String zcurveFile = zcodeSer + resolution + "-" + limit + ".ser";
//            File tempFile = new File(zcurveFile);
//            if (!tempFile.exists()) {// create the z-curve code for each dataset
//                zcurveExist = false;
//                if (dataMapPorto == null) {
////                    因为有bug所以先注释掉
//                    zcodemap = EffectivenessStudy.storeZcurveFile(minx, miny, spaceRange, 2, resolution, zcurveFile, limit, datasetIdMapping);
//                } else
//                    zcodemap = EffectivenessStudy.storeZcurve(minx, miny, spaceRange, 2, dataMapPorto, resolution, zcurveFile);
//            } else {
//                zcurveExist = true;
//                zcodemap = EffectivenessStudy.deSerializationZcurve(zcurveFile);
//            }
//            if (datalakeIndex != null)
//                datalakeIndex.get(1).buildsignarture(zcodemap, datalakeIndex);
//            else {
//                datasetRoot.buildsignarture(zcodemap, datalakeIndex);
//            }
//            for (int j = 0; j < NumberQueryDataset; j++) {
//                testGridOverlapAlone(10, Ri, j + limit, 0);// 8, increase the grid-size to observe the performance
//            }
//        }
//        recordRunningTime("./logs/spadas/efficiency/" + datalakeID + "-r-topk-grid.txt", 0);
    }

    /*
     * evaluate the effect of index's memory mode on the performance
     */
    static void EvaluateIndex(int option) throws IOException {
//        timeMultiple = new double[6][numberofComparisons * numberofParameterChoices];
//        indexMultiple = new double[2][numberofComparisons * numberofParameterChoices];
//        String fileNameString;
//        if (option == 1) {
//            fileNameString = "d";
//        } else {
//            fileNameString = "N";
//        }
//        for (int i = 0; i < numberofParameterChoices; i++) { // test the scale
//            int N = limit; // or change to dimension test
//            if (option == 1) {
//                dimension = ds[i];
//                dimensionAll = false;
//                dimNonSelected = new boolean[11];
//                for (int p = dimension; p < 11; p++)
//                    dimNonSelected[p] = true;// non selected
//            } else {
//                N = (int) (ns[i] * limit) + 10;
//            }
//            System.out.println("datalake scale: " + N);
//            long startTime1a = System.nanoTime();
//            // ** run all in memory, and run top-k Hausdorff and range query
//            storeAllDatasetMemory = true; // load most datasets and create index
//            storeIndexMemory = true; // do not create index
//            datalakeIndex = null;
//            indexMap = null;
//            dataMapPorto = null;
//            readDatalake(N);// read data, create data set index
//            if (dimension > 2)// read and set weight
//                AdvancedHausdorff.setWeight(dimension, indexString + "weight.ser");
//            indexNodesAll = new ArrayList<>(indexNodes);
//            createDatalake(N); // create datalake index
//            long endtimea = System.nanoTime();
//            double size = (getDatalakeSize(dimension) + getDatalakeIndexSize(datasetRoot, false) + getAllDatasetIndexSize()) / (1024.0 * 1024.0);
//            indexMultiple[0][i * numberofComparisons + 1] += size;
//            System.out.println("1index size is: " + size);
//            indexMultiple[1][i * numberofComparisons + 1] += (endtimea - startTime1a) / 1000000000.0;
//            for (int j = 0; j < NumberQueryDataset; j++) {
//                testTopkHausdorff(10, i, j + N, 0, N); // 6, test the top-k Hausdorff
//                testRangeQuery(10, i, j + N, 1); // 7, test the range query
//            }
//
//            // store dataset in memory, load datalake index from disk
//            startTime1a = System.nanoTime();
//            readDatalake(N);
//            File tempFile = new File(indexString + "datalake" + N + "-" + capacity + "-" + dimension + ".txt");
//            if (!tempFile.exists()) {
//                storeIndexMemory = true;
//            } else {
//                storeIndexMemory = false;
//            }
//            createDatalake(N);// create or load the index of whole data lake, and store it into disk to avoid
//            endtimea = System.nanoTime();
//            size = (getDatalakeSize(dimension) + getDatalakeIndexSizeDisk(dimension)) / (1024.0 * 1024);
//            System.out.println("2index size is: " + size);
//            indexMultiple[0][i * numberofComparisons + 2] += size;
//            indexMultiple[1][i * numberofComparisons + 2] += (endtimea - startTime1a) / 1000000000.0;
//            for (int j = 0; j < NumberQueryDataset; j++) {
//                testTopkHausdorff(10, i, j + N, 2, N); // 6, test the top-k Hausdorff
//                testRangeQuery(10, i, j + N, 3); // 7, test the range query
//            }
//
//            // just load the datalake index
//            if (datalakeID <= 2)
//                continue;
//            dataMapPorto = null;
//            datalakeIndex = null;
//            storeAllDatasetMemory = false;
//            buildOnlyRoots = true; // no dataset index
//            startTime1a = System.nanoTime();
//            tempFile = new File(indexString + "datalake" + N + "-" + capacity + "-" + dimension + ".txt");
//            if (!tempFile.exists()) {
//                storeIndexMemory = true;
//            } else {
//                storeIndexMemory = false;
//            }
//            createDatalake(N);// create or load the index of whole data lake, and store it into disk to avoid
//            endtimea = System.nanoTime();
//            System.out.println("size of index is " + datalakeIndex.size());
//            size = (getDatalakeIndexSizeDisk(dimension)) / (1024.0 * 1024);
//            System.out.println("3index size is: " + size);
//            indexMultiple[0][i * numberofComparisons + 3] += size;
//            indexMultiple[1][i * numberofComparisons + 3] += (endtimea - startTime1a) / 1000000000.0;
//            for (int j = 0; j < NumberQueryDataset; j++) {
//                testTopkHausdorff(10, i, j + N, 4, N); // 6, test the top-k Hausdorff
//                testRangeQuery(10, i, j + N, 5); // 7, test the range query
//            }
//        }
//        //write the log files, running time by extracting the columns
//        recordIndexModeEffect("./logs/spadas/efficiency/" + datalakeID + "-M-" + fileNameString + "-topk.txt", "./logs/spadas/efficiency/" + datalakeID + "-M-" + fileNameString + "-range.txt");
//        recordIndexPerformance("./logs/spadas/efficiency/" + datalakeID + "-" + fileNameString + "-MemorySize.txt", 0, indexMultiple);
//        recordIndexPerformance("./logs/spadas/efficiency/" + datalakeID + "-" + fileNameString + "-ConstructionCost.txt", 1, indexMultiple);
    }

    /*
     * test all the functions related to Hausdorff, creating index,
     * pair-wise computation, top-k, join, outlier, restored index
     */
    public static void main(String[] args) throws IOException {
//
//        //	generateIndexFeatures(); used for extract features
//        if (args.length > 1)
//            limit = Integer.valueOf(args[1]); //specifying the length
//        int temp = limit;
//
//        //	limit = 10000;
//        //TODO remarked temporarily
////		System.out.println("testing datalake with scale "+limit);
////		aString = args[0]; // the dataset folder or file
//		/*
//		testFullDataset = true
//		readDatalake(limit);
//		testFullDataset = false;*/
//	/*
//		// 1. pair-wise evaluation
//		storeAllDatasetMemory = true; // load most datasets and create index
//		storeIndexMemory = false; // do not create index
//		zcurveExist = false;
//		readDatalake(limit);//read data, create index if not
//		if(dimension>2)// read and set weight
//			AdvancedHausdorff.setWeight(dimension, indexString + "weight.ser");
//		EvaluatePairWise(); // 1, preparing large datasets by increasing the scale, and evaluate pairwise comparison, slowest
//		EvaluateHausCapacity(); // 2, evaluate the capacity in pair-wise Hausdorff distance.
//		*/
//
//        limit = temp; // in case that limit changed
//        zcodeSer = "./index/dss/index/argo/argo_bitmaps";
//        indexString = "./index/dss/index/argo/";
//
//        //  2. top-k evaluation
//        if (datalakeID <= 2)
//            storeAllDatasetMemory = true;
//        else {// datasets are large
//            storeAllDatasetMemory = false; // no data in memory
//            dataMapPorto = null;
//            indexMap = null;
//        }
//        String zcurveFile = zcodeSer + resolution + "-" + limit + ".ser";
//        File tempFile = new File(zcurveFile);
//        if (!tempFile.exists()) {// create the z-curve code for each dataset
//            zcurveExist = false;
//        } else {
//            zcurveExist = true;
//            zcodemap = EffectivenessStudy.deSerializationZcurve(zcurveFile);
//        }
//        tempFile = new File(indexString + "datalake" + limit + "-" + capacity + "-" + dimension + ".txt");
//        if (!tempFile.exists()) {
//            storeIndexMemory = true;// has to create index as the datalake index is not there
//            if (limit >= 100000 && datalakeID > 2)// for large datasets
//                buildOnlyRoots = true;
//        } else {
//            storeIndexMemory = false;
//            if (datalakeID <= 2)// has to create index for trajectory dataset
//                storeIndexMemory = true;
//        }
//        //long startTime1a = System.nanoTime(); // exact search: 0, 1, 0
//        readDatalake(limit);// read the first
//
////        TimeUnit.MINUTES.sleep(1);
//
//        indexNodesAll = new ArrayList<>(indexNodes);
//        if (!zcurveExist)
//            EffectivenessStudy.SerializedZcurve(zcurveFile, zcodemap);
//        //long endtimea = System.nanoTime();
//        //write("./logs/spadas/efficiency/DatasetIndexTime.txt", datalakeID+","+limit+","+dimension+","+capacity+","+(endtimea-startTime1a)/1000000000.0+"\n");
//        //	write("./logs/spadas/efficiency/DatasetIndexSize.txt", datalakeID+","+limit+","+dimension+","+capacity+","+(getAllDatasetIndexSize())/(1024.0*1024)+"\n");
//
//        //startTime1a = System.nanoTime();
//        //System.out.println(minx+","+miny+","+spaceRange+","+resolution);
//        createDatalake(limit);// create or load the index of whole data lake, and store it into disk to avoid rebuilding it
//        //endtimea = System.nanoTime();
//        //write("./logs/spadas/efficiency/DatalakeIndexTime.txt", datalakeID+","+limit+","+dimension+","+capacity+","+(endtimea-startTime1a)/1000000000.0+"\n");
//        //write("./logs/spadas/efficiency/DatasetIndexSize.txt", datalakeID+","+limit+","+dimension+","+capacity+","+(getDatalakeIndexSize(datasetRoot, false)+getAllDatasetIndexSize())/(1024.0*1024)+"\n");
////        EvaluatePairWise();
//
//        //EvaluateRangequery(); // 3, evaluate range query by increasing range
//        //EvaluateTopkHaus(); // 4, evaluate top-k query by varying k, dimension, resolution
//		/*EvaluateHausDimension(); // 5, evaluate dimension
//
//		if(datalakeID==5) {// restore the parameter
//			dimension = 11;
//			dimensionAll = true;
//		}*/
//		/*
//		// 3. varying datalake scale (number of datasets) to test
//		limit = temp; // in case that limit changed
//		EvaluateDatalakeScale();
//
//		// 4. capacity's effect to datalake index
//		limit = temp; // in case that limit changed
//		EvaluateDatalakeCapacity();
//
//		// 5. evaluate the resolution's effect on grid-based overlap
//		limit = temp; // in case that limit changed
//		EvaluateGridResolution();
//		*/
//
//
//        // 6. evaluate the index mode's effect on construction and top-k Hausdorff and range query
//	/*
//	  	limit = temp;
//		EvaluateIndex(0); // changing N
//		if(datalakeID == 5)
//			EvaluateIndex(1); //*/// changing dimension
    }
}
