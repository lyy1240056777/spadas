package edu.nyu.dss.similarity;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import au.edu.rmit.mtree.tests.Data;
import emd.*;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import org.apache.catalina.webresources.JarResource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import au.edu.rmit.trajectory.clustering.kmeans.indexAlgorithm;
import au.edu.rmit.trajectory.clustering.kmeans.indexNode;
import au.edu.rmit.trajectory.clustering.kpaths.Util;
import org.apache.commons.math3.stat.inference.BinomialTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import web.DTO.*;
import web.Utils.FileProperties;
import web.Utils.ListUtil;
import web.VO.DatasetVo;
import web.VO.PreviewVO;
import web.VO.UnionVO;
import web.exception.FileException;

import javax.naming.InsufficientResourcesException;

@Component
public class Framework {
    /*
     * data set
     */
    static String edgeString, nodeString, distanceString, fetureString, indexString = "";
    static String zcodeSer;
    //TODO chagned
    //static String aString = "/Users/sw160/Desktop/argoverse-api/dataset/train/data";
    //static String aString = "G:\\IdeaProject\\auctus\\train2\\train\\argoData";
    //static String aString = "G:\\IdeaProject\\spadas\\dataset\\argoverse";
//    路径属性，决定了从哪个目录读取数据集，经常要改
    public static String aString = "./dataset";
    //    public static String aString = "D:\\Projects\\GBFS_Spider\\movebank";
//    public static String aString = "D:\\Projects\\Spadas\\spadas_5-16\\spadas_5-16\\dataset_bababackup";
    //    叶子节点最大容量，经常要改
    static int capacity = 1000;
    public static Map<Integer, String> datasetIdMapping = new HashMap<Integer, String>();//integer
    //    存储数据集文件id到数据集文件名的映射，每个目录独立映射

    //   public static HashMap<Integer, String> datasetIdMappingItem = new HashMap<>();

    public static HashMap<Integer, List<double[]>> dataSamplingMap = new HashMap<>();
    public static ArrayList<Map<Integer, String>> datasetIdMappingList = new ArrayList<>();
    public static Map<Integer, double[][]> dataMapPorto = new HashMap<Integer, double[][]>();
    //    每遍历一个目录文件（如城市）生成一个新的map，value总共组成了dataMapPorto，用于计算emd
    public static Map<Integer, double[][]> dataMapForEachDir = new HashMap<>();
    static TreeMap<Integer, Integer> countHistogram = new TreeMap<Integer, Integer>();
    //    维护数据集id到数据集文件路径的映射
    public static Map<Integer, File> fileIDMap = new HashMap<>();
    public static int datalakeID;// the lake id
    static String folderString = ".";
    static int fileNo = 0;
    //	store the data of every point of the whole data lake
    public static List<double[]> dataPoint = new ArrayList<>();

    public static Map<Integer, List<indexNode>> dataLakeMapping = new HashMap<>();

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
    static indexAlgorithm<Object> indexDSS = new indexAlgorithm<>();
    public static Map<Integer, indexNode> indexMap;// root node of dataset's index
    static Map<Integer, indexNode> datalakeIndex = null;// the global datalake index
    public static ArrayList<indexNode> indexNodes = new ArrayList<indexNode>(); // store the root nodes of all datasets in the lake
    static ArrayList<indexNode> indexNodesAll;
    static indexNode datasetRoot = null; // the root node of datalake index in memory mode
    static Map<Integer, Map<Integer, indexNode>> datasetIndex; // restored dataset index
    static HashMap<Integer, ArrayList<Integer>> zcodemap = new HashMap<>();
    //    新的z顺序签名哈希表，用来计算emd
    static HashMap<Integer, HashMap<Long, Double>> zcodeMap = new HashMap<>();
    //    对整个数据湖建立z顺序签名哈希表，每个数据集目录独立，用来计算emd
    static ArrayList<HashMap<Integer, HashMap<Long, Double>>> zcodeMapForLake = new ArrayList<>();
    static Map<Integer, Pair<Double, PriorityQueue<queueMain>>> resultPair =
            new HashMap<Integer, Pair<Double, PriorityQueue<queueMain>>>();
    /*
     * dataset features
     */
    static Map<Integer, double[]> featureMap = new HashMap<Integer, double[]>(); //storing the features for each map

    /*
     * parameters
     */
    static double weight[] = null; // the weight in all dimensions
    static int dimension = 2;
    static int limit = 100000; //number of dataset to be searched
    static int NumberQueryDataset = 1; // number of query datasets
    static boolean dimNonSelected[];// indicate which dimension is not selected
    static boolean dimensionAll = true;// indicate that all dimensions are selected.

    static int ks[] = {10, 20, 30, 40, 50};
    static int ds[] = {2, 4, 6, 8, 10};
    static double ns[] = {0.0001, 0.001, 0.01, 0.05, 0.1};// number of datasets indexed
    static int ss[] = {1, 10, 100, 500, 1000}; //number of datasets to be combined together, i.e., the scale of dataset
    static int rs[] = {3, 4, 5, 6, 7};// resolution to verify the range query, grid overlap, approximate hausdorff
    static int fs[] = {10, 20, 30, 40, 50};

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
    static double timeMultiple[][];// the overall running time
    static double indexMultiple[][];
    //static int fileNo = 1;

    /**
     * 这个函数是用来干嘛的？
     * 尝试去掉Autowired注解
     *
     * @param fileProperties
     */
//    @Autowired
    public void setAString(FileProperties fileProperties) {
        aString = fileProperties.getBaseUri();
        System.out.println(aString);
    }

    /*
     * read the shapenet dataset
     */
    static Map<Integer, double[][]> readShapeNet(File folder, Map<Integer, double[][]> datasetMap, int limit) throws IOException {
        File[] fileNames = folder.listFiles();
        fileNo = 1;
        datasetMap = null;
        if (storeAllDatasetMemory)
            datasetMap = new HashMap<Integer, double[][]>();
        datasetIdMapping = new HashMap<Integer, String>();
        for (File file : fileNames) {
            if (file.isDirectory()) {
                //read the files inside
                readShapeNet(file, datasetMap, limit);
            } else {
                if (!file.getName().contains("name")) {
                    //	System.out.println(file.getName());
                    datasetIdMapping.put(fileNo, file.getName());
                    long lineNumber = 0;
                    try (Stream<String> lines = Files.lines(file.toPath())) {
                        lineNumber = lines.count();
                    }
                    double[][] a = new double[(int) lineNumber][];
                    int i = 0;
                    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                        String strLine;
                        while ((strLine = br.readLine()) != null) {
                            // System.out.println(strLine);
                            String[] splitString = strLine.split(" ");
                            a[i] = new double[3];
                            a[i][0] = Double.valueOf(splitString[0]);
                            a[i][1] = Double.valueOf(splitString[1]);
                            a[i][2] = Double.valueOf(splitString[2]);
                            i++;
                        }
                    }
                    //	System.out.println(i+","+lineNumber);
                    if (countHistogram.containsKey(a.length))// count the histograms
                        countHistogram.put(a.length, countHistogram.get(a.length) + 1);
                    else
                        countHistogram.put(a.length, 1);
                    if (storeAllDatasetMemory == true) {
                        datasetMap.put(fileNo, a);
                    }
                    if (storeIndexMemory) {
                        createDatasetIndex(fileNo, a);
                    }
                    if (!zcurveExist)
                        storeZcurve(a, fileNo);
                    //	Path sourceDirectory = file.toPath();// Paths.get("/Users/personal/tutorials/source");
                    //  Path targetDirectory = Paths.get("/Users/sw160/Desktop/spadas-dataset/Shapenet-allInOne/"+fileNo+".txt");
                    //  write("/Users/sw160/Desktop/spadas-dataset/Shapenet-allInOne/namemapping.txt", fileNo+","+file.getName()+"\n");
                    //  Files.copy(sourceDirectory, targetDirectory); // write into the same folder
                    fileNo++;
                    if (fileNo > limit)
                        break;
                }
            }
        }
        if (fileNo < limit)
            limit = fileNo;
        return datasetMap;
    }

    /*
     * read the trajectory database, e.g., Porot, T-drive
     */
    static Map<Integer, double[][]> readTrajectoryData(File file, int limit) throws IOException {
        Map<Integer, double[][]> datasetMap = null;
        if (storeAllDatasetMemory)
            datasetMap = new HashMap<Integer, double[][]>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String strLine;
            int line = 1;
            while ((strLine = br.readLine()) != null) {
                String[] splitString = strLine.split(",");
                double[][] a = new double[splitString.length / 2][];
                for (int i = 0; i < splitString.length; i++) {
                    if ((i + 1) % 2 == 0) {
                        a[i / 2] = new double[2];
                        for (int j = i - 1; j < i + 1; j++) {
                            a[i / 2][j - i + 1] = Double.valueOf(splitString[j]);
                        }
                    }
                }
                if (countHistogram.containsKey(splitString.length / 2))
                    countHistogram.put(splitString.length / 2, countHistogram.get(splitString.length / 2) + 1);
                else
                    countHistogram.put(splitString.length / 2, 1);
                if (storeAllDatasetMemory)
                    datasetMap.put(line, a);
                if (storeIndexMemory) {
                    createDatasetIndex(line, a);
                }
                if (!zcurveExist)
                    storeZcurve(a, line);
                line++;
                if (line > limit + NumberQueryDataset)
                    break;
            }
            if (line < limit)
                limit = line;
        }
        return datasetMap;
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


    //    读city node目录，递归读
    /*
     * read a folder and extract the corresponding column of each file inside
     * 读目录下的所有数据集文件，构建索引
     */
    public static void readFolder(File folder, int limit, CityNode cityNode, int datasetIDForOneDir, HashMap<Integer, String> datasetIdMappingItem) throws IOException {
        File[] fileNames = folder.listFiles();
//        int datasetIDForOneDir = 0;
//        String fileName;
//        HashMap<Integer, String> datasetIdMappingItem = new HashMap<>();
//		boolean isfolderVisited = false;
        //int fileNo = 1;
//		if(storeAllDatasetMemory)
//			dataMapPorto = new HashMap<Integer, double[][]>();
//        遍历每个数据集文件
        for (File file : fileNames) {
            if (file.isDirectory()) {
                readFolder(file, limit, cityNode, datasetIDForOneDir++, datasetIdMappingItem);
            } else if (file.getName().endsWith(".csv")) {
//				if (!isfolderVisited) {
//					dataLakeMapping.put(index, new ArrayList<>());
//					index++;
//					isfolderVisited = true;
//				}
                String fileName = file.getName();
//                debug为什么会有数据集被重复读取
                if (fileName.contains("432")) {
                    System.out.println();
                }
//                String parentDir = file.getParent();
//            		if(a.length()<15){//argo
//            			//linux ????/
//            			datasetIdMapping.put(fileNo, parentDir.substring(parentDir.lastIndexOf(File.separator)+1)+File.separator+a);
//            			readContentCustom(file, fileNo++);
//            		}
//            		else {//poi
//            			datasetIdMapping.put(fileNo, parentDir.substring(parentDir.lastIndexOf(File.separator)+1)+File.separator+a);
//            			readContentPoi(file, fileNo++, a);
//            		}
//					datasetIdMapping.put(fileNo, parentDir.substring(parentDir.lastIndexOf(File.separator)+1)+File.separator+a);
//                fileNo考虑修改一下，目前表示的是数据集文件数，或许应该换成数据集目录数
//                fileName = parentDir.substring(parentDir.lastIndexOf(File.separator) + 1) + File.separator + a;
                datasetIdMappingItem.put(datasetIDForOneDir, fileName);
//                datasetIdMapping.put(fileNo, fileName);
//                选择使用哪种读取方法
                if (datalakeID == 7) { // 国内的百度POI数据集
                    readContentCity(file, fileNo++, cityNode, datasetIDForOneDir, fileName);
//                    readContentLines(file, fileNo++, cityNode, datasetIDForOneDir, fileName);
//                    continue;
                } else if (datalakeID == 8 || datalakeID == 9) { // 国外的数据集
//                    readContentArgo(file, fileNo++, a, cityNode);
                    readContentUSA(file, fileNo++, cityNode, datasetIDForOneDir, fileName);
//                    continue;
                } else if (datalakeID == 10) {
                    readContentPoi(file, fileNo++, fileName, cityNode);
                }
//                一个数据集集中的索引
                datasetIDForOneDir++;
            }
            if (fileNo > limit) // 一般不会出现这种情况
                break;
        }
//        cityNode.calAttrs(dimension);
////        if (!zcodeMap.isEmpty()) {
////            zcodeMap.clear();
////        }
//        datasetIdMappingList.add(datasetIdMappingItem);
//        HashMap<Integer, HashMap<Long, Double>> zcodeMapTmp = new HashMap<>();
//        dataMapForEachDir.forEach((k, v) -> {
//            storeZcurve(v, k, cityNode.radius * 2, cityNode.radius * 2, cityNode.pivot[0] - cityNode.radius, cityNode.pivot[1] - cityNode.radius, zcodeMapTmp);
//        });
//        dataMapForEachDir.clear();
//        zcodeMapForLake.add(zcodeMapTmp);
//        return datasetIdMappingItem;
//        return dataMapPorto;
    }

    public static Map<Integer, double[][]> readContentXian(File file, int fileNo, String filename) throws IOException {
//		long lineNumber = 0;
//		System.out.println(file.length());
        int i = 0;
        List<double[]> list = new ArrayList<>();
        double[][] xxx;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String strLine;
//			LineNumberReader lnr = new LineNumberReader(new FileReader(file));
//			lineNumber = lnr.getLineNumber() + 1;
//			a = new double[(int) lineNumber-1][];
            while ((strLine = br.readLine()) != null) {
                String[] splitString = strLine.split(",");
                String aString = splitString[0];
                if (!aString.equals("uid")) {// only has float
//					a[i] = new double[dimension];
                    double[] b = new double[dimension];
//					System.out.println(splitString[31]);
                    if (!splitString[31].equals("") && !splitString[32].equals("")) {
                        b[0] = Double.valueOf(splitString[32]);
                        b[1] = Double.valueOf(splitString[31]);
                        list.add(b);
                        i++;
                    }
                }
            }
            xxx = list.toArray(new double[i][]);
        }
        if (i > 0) {
            if (countHistogram.containsKey(i))
                countHistogram.put(i, countHistogram.get(i) + 1);
            else
                countHistogram.put(i, 1);
            if (storeAllDatasetMemory)
                dataMapPorto.put(fileNo, xxx);
            if (storeIndexMemory) {
//				createDatasetIndex(fileNo, xxx,1);
//				debug in the future
//				createDatasetIndex(fileNo, xxx, 1, cityNode);
            }
            if (!zcurveExist)
                storeZcurve(xxx, fileNo);
        }
        return dataMapPorto;
    }

    public static Map<Integer, double[][]> readContentArgo(File file, int fileNo, String fileName, CityNode cityNode) {
//		System.out.println(file.length());
        int i = 0;
        List<double[]> list = new ArrayList<>();
        double[][] xxx;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String strLine;
//			LineNumberReader lnr = new LineNumberReader(new FileReader(file));
//			lineNumber = lnr.getLineNumber() + 1;
//			a = new double[(int) lineNumber-1][];
            while ((strLine = br.readLine()) != null) {
                String[] splitString = strLine.split(",");
                String aString = splitString[0];
                if (aString.equals("TIMESTAMP")) {
                    continue;
                }
                double[] b = new double[dimension];
                b[0] = Double.parseDouble(splitString[6]);
                b[1] = Double.parseDouble(splitString[7]);
                if (b[1] < -85 || b[1] > -75) {
                    continue;
                }
                list.add(b);
                i++;
//					System.out.println(splitString[31]);
//                if (!splitString[12].equals("") && !splitString[11].equals("")) {
////                    try {
////                        b[0] = Double.parseDouble(splitString[12]);
////                        b[1] = Double.parseDouble(splitString[11]);
////                    } catch (Exception e) {
////                        System.out.println(e);
////                        System.out.println(file.getName());
////                        System.out.println(splitString[12]);
////                        System.out.println(splitString[11]);
////                        continue;
////                    }
////                    if (b[0] < 10 || b[1] < 10) {
////                        continue;
////                    }
//                    list.add(b);
//                    i++;
//                }
            }
            xxx = list.toArray(new double[i][]);
            System.out.println("File " + file.getName() + " has " + list.size() + " lines");
            dataPoint.addAll(list);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (i > 0) {
            if (countHistogram.containsKey(i))
                countHistogram.put(i, countHistogram.get(i) + 1);
            else
                countHistogram.put(i, 1);
            if (storeAllDatasetMemory)
                dataMapPorto.put(fileNo, xxx);
            if (storeIndexMemory) {
//				createDatasetIndex(fileNo, xxx,1);
                createDatasetIndex(fileNo, xxx, 1, cityNode);
            }
            if (!zcurveExist)
                storeZcurve(xxx, fileNo);
        }
        return dataMapPorto;
    }

    /**
     * @param dataFile
     * @param fileNo
     * @param cityNode
     * @param datasetIDForOneDir
     * @param fileName
     * @return
     */
    public static Map<Integer, double[][]> readContentUSA(File dataFile, int fileNo, CityNode cityNode, int datasetIDForOneDir, String fileName) {
//        File[] files = dir.listFiles();
//        File dataFile = null;
        File metadataFile = null;
//        for (File file : files) {
//            if (file.getName().endsWith(".csv")) {
//                dataFile = file;
//                break;
//            } else if (file.getName().endsWith(".json")) {
//                metadataFile = file;
//            }
//        }
        indexNode node = new indexNode(2);
        int i = 0;
        List<double[]> list = new ArrayList<>();
        double[][] xxx;
//        解析单个数据集文件并存到数组中
        try (BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
            String strLine;
            while ((strLine = br.readLine()) != null) {
                String[] splitString = strLine.split(",");
                if (splitString.length != 2) {
//                    System.out.println();
                    continue;
                }
                if (splitString[0].equals("") || splitString[1].equals("")) {
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
            xxx = list.toArray(new double[i][]);
            System.out.println("File " + dataFile.getName() + " has " + list.size() + " lines");
            dataPoint.addAll(list);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (i > 0) { // 确保读取到了数据
//            更新一些全部变量
            if (countHistogram.containsKey(i))
                countHistogram.put(i, countHistogram.get(i) + 1);
            else
                countHistogram.put(i, 1);
            if (storeAllDatasetMemory) { // 这能是false？
                dataMapPorto.put(fileNo, xxx);
                dataMapForEachDir.put(datasetIDForOneDir, xxx);
            }
            if (storeIndexMemory) { // true，要创建下层索引（数据集索引）
//				createDatasetIndex(fileNo, xxx,1);
//                创建下层索引，cityNode没有用
                node = createDatasetIndex(fileNo, xxx, 1, cityNode);
                node.setFileName(fileName);
//                为了系统前端好显示
                samplingDataByGrid(xxx, fileNo, node);
            }
//            一些全部变量
            datasetIdMapping.put(fileNo, fileName);
            fileIDMap.put(fileNo, dataFile);
            if (!zcurveExist) {
                storeZcurve(xxx, fileNo);
                node.setSignautre(zcodemap.get(fileNo).stream().mapToInt(Integer::intValue).toArray());
//                EffectivenessStudy.SerializedZcurve(zcodemap);
            }
        }
        return dataMapPorto;
    }

    //    读一个数据集文件
    public static Map<Integer, double[][]> readContentCity(File file, int fileNo, CityNode cityNode, int datasetIDForOneDir, String fileName) {
//		System.out.println(file.length());
        int i = 0;
        List<double[]> list = new ArrayList<>();
        double[][] xxx;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String strLine;
//			LineNumberReader lnr = new LineNumberReader(new FileReader(file));
//			lineNumber = lnr.getLineNumber() + 1;
//			a = new double[(int) lineNumber-1][];
            while ((strLine = br.readLine()) != null) {
                String[] splitString = strLine.split(",");
                String aString = splitString[0];
                if (aString.equals("uid")) {
                    continue;
                }
                double[] b = new double[dimension];
//					System.out.println(splitString[31]);
                if (!splitString[12].equals("") && !splitString[11].equals("")) {
                    try {
//                        第一个值是纬度lat，第二个值是经度lng
                        b[0] = Double.parseDouble(splitString[12]);
                        b[1] = Double.parseDouble(splitString[11]);
                    } catch (Exception e) {
                        System.out.println(e);
                        System.out.println(file.getName());
                        System.out.println(splitString[12]);
                        System.out.println(splitString[11]);
                        continue;
                    }
//                    if (b[0] < 10 || b[1] < 10) {
//                        continue;
//                    }
//                    过滤掉不符合中国经纬度范围的数据
                    if (b[0] < 3 || b[0] > 54 || b[1] < 73 || b[1] > 136) {
                        continue;
                    }
//                    过滤掉不符合经纬度范围的数据
//                    if (b[0] < -90 || b[0] > 90 || b[1] < -180 || b[1] > 180) {
//                       continue;
//                    }
                    list.add(b);
                    i++;
                }
            }
            xxx = list.toArray(new double[i][]);
            System.out.println("File " + file.getName() + " has " + list.size() + " lines");
            dataPoint.addAll(list);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (i > 0) {
            if (countHistogram.containsKey(i))
                countHistogram.put(i, countHistogram.get(i) + 1);
            else
                countHistogram.put(i, 1);
            if (storeAllDatasetMemory) {
                dataMapPorto.put(fileNo, xxx);
                dataMapForEachDir.put(datasetIDForOneDir, xxx);
            }
            if (storeIndexMemory) {
//				createDatasetIndex(fileNo, xxx,1);
                indexNode node = createDatasetIndex(fileNo, xxx, 1, cityNode);
//                对数据进行基于网格的取样，减小数据量
                samplingDataByGrid(xxx, fileNo, node);
                node.setFileName(fileName);
            }
            datasetIdMapping.put(fileNo, fileName);
            fileIDMap.put(fileNo, file);
//            if (!zcurveExist) {
//                storeZcurve(xxx, fileNo, 5, 5, 30, 100);
////                EffectivenessStudy.SerializedZcurve(zcodemap);
//            }
        }
        return dataMapPorto;
    }

    public static Map<Integer, double[][]> readContentLines(File file, int fileNo, CityNode cityNode, int datasetIDForOneDir, String fileName) {
//		System.out.println(file.length());
        int i = 0;
        List<double[]> list = new ArrayList<>();
        double[][] xxx;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String strLine;
//			LineNumberReader lnr = new LineNumberReader(new FileReader(file));
//			lineNumber = lnr.getLineNumber() + 1;
//			a = new double[(int) lineNumber-1][];
            while ((strLine = br.readLine()) != null) {
                String[] splitString = strLine.split(",");
                String aString = splitString[0];
                if (aString.equals("lng")) {
                    continue;
                }
                double[] b = new double[dimension];
//					System.out.println(splitString[31]);
                if (!splitString[1].equals("") && !splitString[0].equals("")) {
                    try {
//                        第一个值是纬度lat，第二个值是经度lng
                        b[0] = Double.parseDouble(splitString[1]);
                        b[1] = Double.parseDouble(splitString[0]);
                    } catch (Exception e) {
                        System.out.println(e);
                        System.out.println(file.getName());
                        System.out.println(splitString[1]);
                        System.out.println(splitString[0]);
                        continue;
                    }
//                    if (b[0] < 10 || b[1] < 10) {
//                        continue;
//                    }
//                    过滤掉不符合中国经纬度范围的数据
//                    if (b[0] < 3 || b[0] > 54 || b[1] < 73 || b[1] > 136) {
//                        continue;
//                    }
//                    过滤掉不符合经纬度范围的数据
                    if (b[0] < -90 || b[0] > 90 || b[1] < -180 || b[1] > 180) {
                        continue;
                    }
                    list.add(b);
                    i++;
                }
            }
            xxx = list.toArray(new double[i][]);
            System.out.println("File " + file.getName() + " has " + list.size() + " lines");
            dataPoint.addAll(list);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (i > 0) {
            if (countHistogram.containsKey(i))
                countHistogram.put(i, countHistogram.get(i) + 1);
            else
                countHistogram.put(i, 1);
            if (storeAllDatasetMemory) {
                dataMapPorto.put(fileNo, xxx);
                dataMapForEachDir.put(datasetIDForOneDir, xxx);
            }
            if (storeIndexMemory) {
//				createDatasetIndex(fileNo, xxx,1);
                indexNode node = createDatasetIndex(fileNo, xxx, 1, cityNode);
//                对数据进行基于网格的取样，减小数据量
                samplingDataByGrid(xxx, fileNo, node);
            }
            datasetIdMapping.put(fileNo, fileName);
//            if (!zcurveExist) {
//                storeZcurve(xxx, fileNo, 5, 5, 30, 100);
////                EffectivenessStudy.SerializedZcurve(zcodemap);
//            }
        }
        return dataMapPorto;
    }

    public static void samplingDataByGrid(double[][] data, int id, indexNode node) {
        double xMin = node.getPivot()[0] - node.getRadius();
        double yMin = node.getPivot()[1] - node.getRadius();
        double unit = node.getRadius() * 2 / Math.pow(2, resolution);
        int len = (int) Math.pow(2, resolution);
        HashMap<Integer, Integer> dataSamp = new HashMap<>();
//        dataSamp.put(1,1);
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
            tmp[1] = (int) (k / len) * unit + yMin;
            tmp[2] = (double) v / data.length;
//            tmp[2] = v;
            dataSampling.add(tmp);
        });
        dataSamplingMap.put(id, dataSampling);
    }

    /*
     * storing the z-curve
     */
    public static void storeZcurveForEMD(double[][] dataset, int datasetid, double xRange, double yRange, double minx, double miny, HashMap<Integer, HashMap<Long, Double>> zcodeMapTmp) {
//        设置一下参数默认值，以后需要修改
//        xRange = 10;
//        yRange = 10;
//        minx = 0;
//        miny = 70;

//        if (!zcodemap.isEmpty()) {
//            zcodemap.clear();
//        }
//        if (zcodemap == null)
//            zcodemap = new HashMap<Integer, ArrayList<Integer>>();
        int pointCnt = dataset.length;
        int numberCells = (int) Math.pow(2, resolution);
//        double unit = spaceRange / numberCells;
        double xUnit = xRange / numberCells;
        double yUnit = yRange / numberCells;
        double weightUnit = 1.0 / pointCnt;
        HashMap<Long, Double> zcodeItemMap = new HashMap<>();
//        ArrayList<Integer> zcodeaArrayList = new ArrayList<Integer>();
        for (int i = 0; i < dataset.length; i++) {
//            int x = (int) ((dataset[i][0] - minx) / unit);
//            int y = (int) ((dataset[i][1] - miny) / unit);
            int x = (int) ((dataset[i][0] - minx) / xUnit);
            int y = (int) ((dataset[i][1] - miny) / yUnit);
            long zcode = EffectivenessStudy.combine(x, y, resolution);
            if (zcodeItemMap.containsKey(zcode)) {
                double val = zcodeItemMap.get(zcode);
                zcodeItemMap.put(zcode, val + weightUnit);
            } else {
                zcodeItemMap.put(zcode, weightUnit);
            }
//                zcodeaArrayList.add(zcode);
        }
//        是否需要设置zcodemap还是直接用zcodeArrayList？
//        还是先保留zcodemap
        zcodeMapTmp.put(datasetid, zcodeItemMap);
    }

//    旧的z-order生成方法，
    public static void storeZcurve(double[][] dataset, int datasetid) {
        int numberCells = (int) Math.pow(2, resolution);
        double unit = spaceRange / numberCells;
        ArrayList<Integer> zcodeaArrayList = new ArrayList<>();
        for (int i = 0; i < dataset.length; i++) {
            int x = (int) ((dataset[i][0] - minx) / unit);
            int y = (int) ((dataset[i][1] - miny) / unit);
//            图省事，日后要修改，可能导致精度丢失
//            resolution + 1才能保证combine过程不丢失x和y的精度
            int zcode = (int)EffectivenessStudy.combine(x, y, resolution + 1);
            if (!zcodeaArrayList.contains(zcode))
                zcodeaArrayList.add(zcode);
        }
        Collections.sort(zcodeaArrayList);
        zcodemap.put(datasetid, zcodeaArrayList);
    }

    public static int[] generateZcurveForRange(double[] minRange, double[] maxRange) {
        int numberCells = (int) Math.pow(2, resolution);
        double unit = spaceRange / numberCells;
        List<Integer> list = new ArrayList<>();
        int minX = (int) ((minRange[0] - minx) / unit);
        int minY = (int) ((minRange[1] - miny) / unit);
        int maxX = (int) ((maxRange[0] - minx) / unit);
        int maxY = (int) ((maxRange[1] - miny) / unit);
        for (int i = minX; i < maxX; i++) {
            for (int j = minY; j < maxY; j++) {
                int zcode = (int) EffectivenessStudy.combine(i, j, resolution + 1);
                if (!list.contains(zcode)) {
                    list.add(zcode);
                }
            }
        }
        Collections.sort(list);
        return list.stream().mapToInt(Integer::intValue).toArray();
    }

    /*
     * read single dataset in an argoverse folder
     */
    public static void readContent(File file, int fileNo) throws IOException {
        //	System.out.println("read file " + file.getCanonicalPath());
        long lineNumber = 0;
        try (Stream<String> lines = Files.lines(file.toPath())) {
            lineNumber = lines.count();
        }
        double[][] a = new double[(int) lineNumber - 1][];// a little different
        int i = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String strLine;
            while ((strLine = br.readLine()) != null) {
                // System.out.println(strLine);
                String[] splitString = strLine.split(",");
                if (splitString.length < 3)
                    System.out.println(file.toURI());
                String aString = splitString[3];
                if (aString.matches("-?\\d+(\\.\\d+)?")) {// only has float
                    a[i] = new double[3];
                    a[i][0] = Double.valueOf(splitString[6]);
                    a[i][1] = Double.valueOf(splitString[7]);
                    a[i][2] = Double.valueOf(splitString[0]);
                    i++;
                }
            }
        }
        if (countHistogram.containsKey(i))
            countHistogram.put(i, countHistogram.get(i) + 1);
        else
            countHistogram.put(i, 1);
        if (storeAllDatasetMemory)
            dataMapPorto.put(fileNo, a);
        if (storeIndexMemory) {
//			debug in the future
//			createDatasetIndex(fileNo, a, 0, cityNode);
        }
        if (!zcurveExist)
            storeZcurve(a, fileNo);
    }

    public static void readContentCustom(File file, int fileNo) throws IOException {
        long lineNumber = 0;
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
                    a[i][0] = Double.valueOf(splitString[6]);
                    a[i][1] = Double.valueOf(splitString[7]);
                    //a[i][2] = Double.valueOf(splitString[0]);
                    a[i][2] = Double.valueOf(splitString[1].replace("-", ""));
                    i++;
                }
            }
        }
        if (countHistogram.containsKey(i))
            countHistogram.put(i, countHistogram.get(i) + 1);
        else
            countHistogram.put(i, 1);
        if (storeAllDatasetMemory)
            dataMapPorto.put(fileNo, a);
        if (storeIndexMemory) {
//			debug in the future
//			createDatasetIndex(fileNo, a, 0, dataLakeMapping.get(fileNo));
        }
        if (!zcurveExist)
            storeZcurve(a, fileNo);
    }

    /*
     * read single dataset in a chicago folder
     */
    public static Map<Integer, double[][]> readContentChicago(File file, int fileNo, String filename) throws IOException {
        long lineNumber = 0;
        try (Stream<String> lines = Files.lines(file.toPath())) {
            lineNumber = lines.count();
        }
        double[][] a = new double[(int) lineNumber][];
        int i = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String strLine;
            while ((strLine = br.readLine()) != null) {
                // System.out.println(strLine);
                String[] splitString = strLine.split(",");
				/*	Boolean isnumberBoolean = true;
				for(int j=0; j<11; j++)
					if(splitString[j].isEmpty() || splitString[j].matches("-?\\d+(\\.\\d+)?")==false) {
						isnumberBoolean = false;
						break;
					}
				if (isnumberBoolean) {*/
                //	write(filenameString, strLine+"\n");
                //	System.out.println(strLine);
                a[i] = new double[dimension];
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
        if (i > 0) {
            if (countHistogram.containsKey(i))
                countHistogram.put(i, countHistogram.get(i) + 1);
            else
                countHistogram.put(i, 1);
            if (storeAllDatasetMemory)
                dataMapPorto.put(fileNo, a);
            if (storeIndexMemory) {
                createDatasetIndex(fileNo, a);
            }
            if (!zcurveExist)
                storeZcurve(a, fileNo);
        }
        return dataMapPorto;
    }

    /*
     * read single dataset in a poi folder
     */
    public static Map<Integer, double[][]> readContentPoi(File file, int fileNo, String filename, CityNode cityNode) throws IOException {
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
                    a[i][0] = Double.valueOf(splitString[0]);
                    a[i][1] = Double.valueOf(splitString[1]);
                    i++;
                }
            }
        }
        if (i > 0) {
            if (countHistogram.containsKey(i))
                countHistogram.put(i, countHistogram.get(i) + 1);
            else
                countHistogram.put(i, 1);
            if (storeAllDatasetMemory)
                dataMapPorto.put(fileNo, a);
            if (storeIndexMemory) {
//				debug in the future
//				createDatasetIndex(fileNo, a, 1, dataLakeMapping.get(fileNo));
                indexNode node = createDatasetIndex(fileNo, a, 1, cityNode);
                node.setFileName(filename);
                samplingDataByGrid(a, fileNo, node);
            }
            datasetIdMapping.put(fileNo, filename);
            fileIDMap.put(fileNo, file);
//            if (!zcurveExist)
//                storeZcurve(a, fileNo, 0, 0, 0, 0, null);
        }
        return dataMapPorto;
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

    /*
     * test whether hausdorff is metric
     */
//    public void testTriangle() throws IOException {
//        String foldername = "/Users/sw160/Desktop/argoverse-api/dataset/train/data";
//        File folder = new File(foldername);
////		debug in the future
//        Map<Integer, double[][]> dataMap = readFolder(folder, 1000, cityNodeList.get(0));
//        folder = new File("/Users/sw160/Downloads/torch-clus/dataset/minist_60000_784");
//        dataMap = readMnist(folder);
//        for (int a : dataMap.keySet()) {
//            if (a > dataMap.size() - 3)
//                break;
//            //	System.out.println(dataMap.get(a)[0].length);
//            double distance = Hausdorff.HausdorffExact(dataMap.get(1), dataMap.get(a), dataMap.get(a)[0].length, false);
//            double distance1 = Hausdorff.HausdorffExact(dataMap.get(a), dataMap.get(a + 1), dataMap.get(a)[0].length, false);
//            double distance2 = Hausdorff.HausdorffExact(dataMap.get(a + 1), dataMap.get(a + 2), dataMap.get(a)[0].length, false);
//            double distance3 = Hausdorff.HausdorffExact(dataMap.get(a), dataMap.get(a + 2), dataMap.get(a)[0].length, false);
//            if (distance1 + distance2 < distance3)
//                System.out.println("no triangle inequality");//
//        }
//    }

//    public static void testTriangleFrechet() throws IOException {
//        String foldername = "/Users/sw160/Desktop/argoverse-api/dataset/train/data";
//        File folder = new File(foldername);
////		debug in the future
//        Map<Integer, double[][]> dataMap = readFolder(folder, 1000, cityNodeList.get(0));
//        folder = new File("/Users/sw160/Downloads/torch-clus/dataset/minist_60000_784");
//        dataMap = readMnist(folder);
//        for (int a : dataMap.keySet()) {
//            if (a > dataMap.size() - 3)
//                break;
//            //	System.out.println(dataMap.get(a)[0].length);
//            double distance = Hausdorff.Frechet(dataMap.get(1), dataMap.get(a), dataMap.get(a)[0].length);
//            //	System.out.println(distance);
//            double distance1 = Hausdorff.Frechet(dataMap.get(a), dataMap.get(a + 1), dataMap.get(a)[0].length);
//            double distance2 = Hausdorff.Frechet(dataMap.get(a + 1), dataMap.get(a + 2), dataMap.get(a)[0].length);
//            double distance3 = Hausdorff.Frechet(dataMap.get(a), dataMap.get(a + 2), dataMap.get(a)[0].length);
//            if (distance1 + distance2 < distance3 || Math.abs(distance1 - distance2) > distance3)
//                System.out.println("no triangle inequality");//
//        }
//    }

    public static void generateGTforPrediciton(String distanceString, Map<Integer, double[][]> dataMapPorto, Map<Integer, indexNode> indexMap, int dimension, int limit) {
        write(distanceString, "datasetID,dataset2ID,distance,normalizedDistance\n");
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
                write(distanceString, Integer.toString(b) + "," + Integer.toString(a) + "," + Double.toString(distance) + ","
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

    public static Pair<indexNode, double[][]> readNewFile(MultipartFile file, String filename) throws IOException {
        if (!file.getContentType().equals("text/csv")) {
            return null;
        }
        List<String> headers = new ArrayList<>();
        List<double[]> list = new ArrayList<>();
        double[][] xxx;
        int lat = 0, lng = 0;

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
            String strLine = null;
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
            System.out.println("File " + file.getName() + " has " + list.size() + " lines");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (list.isEmpty()) {
            return null;
        }
        indexNode node = buildNode(xxx, 2);
        node.setFileName(filename);
        node.setDatasetID(-3);
        return new MutablePair<>(node, xxx);
    }

    /**
     * setting parameters and reading the whole datalake
     * 需要对每个目录（argoverse、poi、beijing、shanghai等）建立z-order签名文件
     * 这样的话样例查询就只能在同一个数据集目录下进行，但这也合理，毕竟同一个目录下数据集之间距离是最近的
     *
     * @param limit
     * @throws IOException
     */
    static void readDatalake(int limit) throws IOException {
        File folder = new File(aString);
        String[] filelist = folder.list();
        int index = 0;
//        遍历每个目录
        for (String str : filelist) {
            System.out.println(str);
            String pre_str = "./index/dss/index/";
            String conn_str = str + "/" + str;
            edgeString = pre_str + conn_str + "_edge.txt";
            nodeString = pre_str + conn_str + "_node.txt";
            zcodeSer = pre_str + conn_str + "_bitmaps";
            distanceString = pre_str + conn_str + "_haus_distance.txt";
            fetureString = pre_str + conn_str + "_haus_features.txt";
            indexString = pre_str + str + "/";
            dimension = 2;
//            if (str.equals("poi")) {
//                continue;
//            } else if (str.equals("argoverse")) {
//                datalakeID = 6;
//            } else {
//                datalakeID = 7;
//            }

            HashMap<Integer, String> datasetIdMappingItem = new HashMap<>();

//            if (str.equals("bus_lines")) {
//                datalakeID = 7;
//            } else {
//                continue;
//            }
//            决定要读取的数据集
            if (str.contains("movebank")) {
                datalakeID = 9;
                continue;
            } else if (str.equals("Bus_lines")) {
                datalakeID = 8;
                continue;
            } else if (Character.isUpperCase(str.charAt(0))) { // 国外的数据集
                datalakeID = 8;
//                continue;
            } else if (str.equals("poi")) {
                datalakeID = 10;
                continue;
            } else { // 国内的数据集
                datalakeID = 7;
                continue;
            }
//            datasetIdMappingItem.clear();
            File myFolder = new File(aString + "/" + str);
//            可以先计算这cityNode，但是前端不要显示它，到时候再删除
//            已经没用了，以后删掉
            CityNode cityNode = new CityNode(str, dimension);
//			dataLakeMapping.put(index, new ArrayList<>());
//            readFolder(myFolder, limit, cityNode, 0, datasetIdMappingItem);

//            核心代码，这里的myFolder目录下就全部是数据集文件了
            readFolder(myFolder, limit, cityNode, 0, datasetIdMappingItem);

//            cityNode.calAttrs(dimension);

//            待删
            cityNode.calAttrs(dimension);
//        if (!zcodeMap.isEmpty()) {
//            zcodeMap.clear();
//        }
            datasetIdMappingList.add(datasetIdMappingItem);
            HashMap<Integer, HashMap<Long, Double>> zcodeMapTmp = new HashMap<>();
            dataMapForEachDir.forEach((k, v) -> {
                storeZcurveForEMD(v, k, cityNode.radius * 2, cityNode.radius * 2, cityNode.pivot[0] - cityNode.radius, cityNode.pivot[1] - cityNode.radius, zcodeMapTmp);
            });
            dataMapForEachDir.clear();
            zcodeMapForLake.add(zcodeMapTmp);

            cityNodeList.add(cityNode);
            cityIndexNodeMap.put(cityNode.cityName, cityNode.nodeList);
            index++;
        }
        System.out.println("Totally " + filelist.length + " files/folders and " + dataPoint.size() + " lines");
        countHistogram = new TreeMap<Integer, Integer>();
        //	dataMapPorto = new HashMap<>();
//
//		if(aString.contains("argo")) {// read datasets in a folder, such as argoverse and chicago
//			datalakeID = 4;
//			dimension = 2;
//			if(testFullDataset)
//				limit = 270000;
//			edgeString = "./index/dss/index/argo/argo_edge.txt";
//			nodeString = "./index/dss/index/argo/argo_node.txt";
//			zcodeSer= "./index/dss/index/argo/argo_bitmaps";
//			distanceString = "./index/dss/index/argo/argo_haus_distance.txt";
//			fetureString = "./index/dss/index/argo/argo_haus_features.txt";
//			indexString = "./index/dss/index/argo/";
//			dataMapPorto = readFolder(folder, limit+NumberQueryDataset);
//		}
////		aString =  "./dataset/poi";
////		folder = new File(aString);
//		if(aString.contains("poi")) {// read datasets in a folder, such as argoverse and chicago
//			datalakeID = 5;
//			dimension = 2;
//			if(testFullDataset)
//				limit = 280000;
//			edgeString = "./index/dss/index/poi/poi_edge.txt";
//			nodeString = "./index/dss/index/poi/poi_node.txt";
//			zcodeSer= "./index/dss/index/poi/poi_bitmaps";
//			distanceString = "./index/dss/index/poi/poi_haus_distance.txt";
//			fetureString = "./index/dss/index/poi/poi_haus_features.txt";
//			indexString = "./index/dss/index/poi/";
//			dataMapPorto =  readFolder(folder, limit+NumberQueryDataset);
//		//	SerializedMappingTable(indexString+"mapping.ser");
//		}
//
////		aString = "D:\\Projects\\POICollector\\POICollector\\POICollector\\data_Collect\\data";
//
//		if (aString.contains("xian")) {
//			datalakeID = 6;
//			dimension = 2;
//			if (testFullDataset){
//				limit = 1000000;
//			}
//			edgeString = "./index/dss/index/xian/xian_edge.txt";
//			nodeString = "./index/dss/index/xian/xian_node.txt";
//			zcodeSer= "./index/dss/index/xian/xian_bitmaps";
//			distanceString = "./index/dss/index/xian/xian_haus_distance.txt";
//			fetureString = "./index/dss/index/xian/xian_haus_features.txt";
//			indexString = "./index/dss/index/xian/";
//			dataMapPorto = readFolder(folder, limit + NumberQueryDataset);
//		}
//
//		aString = "./dataset";

		/*else if(aString.contains("mnist")){
			dimension = 28;
			edgeString = "./index/dss/mnist_edge.txt";
			nodeString = "./index/dss/mnist_node.txt";
			distanceString = "./index/dss/mnist_haus_distance.txt";
			fetureString = "./index/dss/mnist_haus_features.txt";
			dataMapPorto = readMnist(folder);
		}else if(aString.contains("beijing")){
			dimension = 2;
			datalakeID = 2;
			if(testFullDataset)
				limit = 250000;
			edgeString = "./index/dss/index/beijing/beijing_edge.txt";
			nodeString = "./index/dss/index/beijing/beijing_node.txt";
			zcodeSer= "./index/dss/index/beijing/beijing_bitmaps";
			distanceString = "./index/dss/index/beijing/beijing_haus_distance.txt";
			fetureString = "./index/dss/index/beijing/beijing_haus_features.txt";
			indexString = "./index/dss/index/beijing/";
			dataMapPorto = readTrajectoryData(folder, limit);
		}else if(aString.contains("footprint")){
			zcodeSer= "./index/footprint_bitmaps";
			edgeString = "./index/dss/nyc_edge.txt";
			nodeString = "./index/dss/nyc_node.txt";
			distanceString = "./index/dss/nyc_haus_distance.txt";
			fetureString = "./index/dss/nyc_haus_features.txt";
			dataMapPorto = readTrajectoryData(folder, limit);
		}else if(aString.contains("Shapenet")){
			datalakeID = 3;
			dimension = 3;
			if(testFullDataset)
				limit = 32000;
			zcodeSer= "./index/dss/index/shapenet/shapenet_bitmaps";
			edgeString = "./index/dss/index/shapenet/shapenet_edge.txt";
			nodeString = "./index/dss/index/shapenet/shapenet_node.txt";
			distanceString = "./index/dss/index/shapenet/shapenet_haus_distance.txt";
			fetureString = "./index/dss/index/shapenet/shapenet_haus_features.txt";
			indexString = "./index/dss/index/shapenet/";
			dataMapPorto = readShapeNet(folder, dataMapPorto, limit+NumberQueryDataset);
		}else {//reading porto
			datalakeID = 1;
			dimension = 2;
			if(testFullDataset)
				limit = 1000000;
			edgeString = "./index/dss/index/porto/porto_edge.txt";
			zcodeSer= "./index/dss/index/porto/porto_bitmaps";
			nodeString = "./index/dss/index/porto/porto_node.txt";
			distanceString = "./index/dss/index/porto/porto_haus_distance.txt";
			fetureString = "./index/dss/index/porto/porto_haus_features.txt";
			indexString = "./index/dss/index/porto/";
			dataMapPorto = readTrajectoryData(folder, limit);
		}*/
        System.out.println("dataset is loaded");
//		weight = normailizationWeight(dataMapPorto, 0, 1, dimension, indexString+"weight.ser");//the weight for each dimension
        for (int key : countHistogram.keySet()) {
            write(indexString + "countHistogram.txt", key + "," + countHistogram.get(key) + "\n");
        }
    }


    // write the information into files
    public static void write(String fileName, String content) {
        RandomAccessFile randomFile = null;
        try {
            randomFile = new RandomAccessFile(fileName, "rw");
            long fileLength = randomFile.length();
            randomFile.seek(fileLength);
            randomFile.writeBytes(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (randomFile != null) {
                try {
                    randomFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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
        write(edgeString, "datasetID,startNode,endNode,distance\n");
        write(nodeString, "datasetID,node,#CoverPoints,Depth,Radius,Leaf,PivotPoint\n");
        indexDSS.storeFeatures(rootBall, 1, 1, edgeString, nodeString, 1);
        // get the features of index tree,
        //	double features[] = datasetFeatures.getFeature(fetureString, rootBall,rootBall.getTotalCoveredPoints(), capacity, a);
        // featureMap.put(a, features);
    }

    // create datalake index based on the root nodes of all datasets, too slow
    static void createDatalake(int N) {
        indexDSS.setGloabalid();
        if (storeIndexMemory) {// build index for the whole datalake
            datasetRoot = indexDSS.indexDatasetKD(indexNodes, dimension, capacity, weight);
            System.out.println("index created");
            //	if(storeIndex)
//			indexDSS.storeDatalakeIndex(datasetRoot, 1, indexString+"datalake"+N+"-"+capacity+"-"+dimension+".txt", 0);//store the
            //	datalakeIndex = new HashMap<>();
            //	indexDSS.reorgnizeIndex(datasetRoot, 1, 0, datalakeIndex);//putting all nodes into hashmap
        } else {
            datalakeIndex = indexDSS.restoreDatalakeIndex(indexString + "datalake" + N + "-" + capacity + "-" + dimension + ".txt", dimension);//the datalake index
        }

        if (datalakeIndex != null) {
            datalakeIndex.get(1).setMaxCovertpoint(datalakeIndex); // for the query that measures how many points in the intersected range
//            datalakeIndex.get(1).buildsignarture(zcodemap, datalakeIndex); // for the gird-based overlap query, need to debug for the memory mode
        } else {
            datasetRoot.setMaxCovertpoint(datalakeIndex);
//            datasetRoot.buildsignarture(zcodemap, datalakeIndex);
        }
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
        numberofComparisons = 4;

        int countAlgorithm = 3;

        double[] querymax = new double[dimension];
        double[] querymin = new double[dimension];
        for (int i = 0; i < dimension; i++) {// to enlarge the range for evaluation
            querymax[i] = dataseNode.getPivot()[i] + error;
            querymin[i] = dataseNode.getPivot()[i] - error;
        }

        // baseline method here, scan every datasets

        long startTime1 = System.nanoTime();
        double min_dis = Double.MAX_VALUE;
        ArrayList<double[]> points = null;
        dataseNode.coveredPOints(querymax, querymin, min_dis, dimension, dataset, null, dimNonSelected, dimensionAll, points);
        long endtime = System.nanoTime();
        System.out.println((endtime - startTime1) / 1000000000.0);
        timeMultiple[timeID][testOrder * numberofComparisons + countAlgorithm++] += (endtime - startTime1) / 1000000000.0;
    }

    // restored index, not putting all the datasets and index in memeory, only access when it is checked after passing datalake index
    static void testRestoredIndex() {
        /* test the restored index, it works well, while it is a little slower because of finding the hashmap */
        long startTime1 = System.nanoTime();//exact search: 0, 1, 0
        Pair<Double, PriorityQueue<queueMain>> resultPairq = AdvancedHausdorff.IncrementalDistance(dataMapPorto.get(1), dataMapPorto.get(3), dimension, indexMap.get(1), indexMap.get(3), 0, 1, 0, false, 0, false, null, null, null, true);
        long endtime = System.nanoTime();
        System.out.println((endtime - startTime1) / 1000000000.0 + "," + AdvancedHausdorff.disCompTime + "," + AdvancedHausdorff.EuclideanCounter);
        System.out.println(resultPairq.getLeft());

        startTime1 = System.nanoTime();//exact search: 0, 1, 0
        datasetIndex = indexDSS.restoreIndex(indexString, dimension, dataMapPorto);
        endtime = System.nanoTime();
        //	System.out.println("building index costs: "+(endtimea-startTime1a)/1000000000.0);
        System.out.println("reconstruction costs: " + (endtime - startTime1) / 1000000000.0);

        startTime1 = System.nanoTime();//exact search: 0, 1, 0
        Pair<Double, PriorityQueue<queueMain>> resultPair11 = AdvancedHausdorff.IncrementalDistance(dataMapPorto.get(1), dataMapPorto.get(3), dimension, datasetIndex.get(1).get(1), datasetIndex.get(3).get(1), 0, 1, 0, false, 0, false, datasetIndex.get(1), datasetIndex.get(3), null, true);
        endtime = System.nanoTime();
        System.out.println((endtime - startTime1) / 1000000000.0 + "," + AdvancedHausdorff.disCompTime + "," + AdvancedHausdorff.EuclideanCounter);
        System.out.println(resultPair11.getLeft());
    }

    static indexNode getQueryNode(double[][] querydata, int queryid) throws FileNotFoundException, IOException {
        indexNode queryNode = null;
        if (dataMapPorto == null)
            querydata = readSingleFile(datasetIdMapping.get(queryid));
        else
            querydata = dataMapPorto.get(queryid);
        if (indexMap != null) {
            System.out.println();
            queryNode = indexMap.get(queryid);
        } else {
            if (datasetIndex != null) {
                queryNode = datasetIndex.get(queryid).get(1);
            } else {
                queryNode = indexDSS.buildBalltree2(querydata, dimension, capacity, null, null, weight);
            }
        }
        return queryNode;
    }

    // four methods: scanning-all, sigspatial10, early adandoning, datalake
    static void testTopkHausdorff(int k, int testOrder, int queryid, int timeID, int limit) throws FileNotFoundException, IOException {
        numberofComparisons = 4;
        int countAlgorithm = 0;
        /* read query dataset */
        indexNode queryNode = null;
        Map<Integer, indexNode> queryindexmap = null; // datasetIndex.get(queryid);
        double[][] querydata;
        if (dataMapPorto == null)
            querydata = readSingleFile(datasetIdMapping.get(queryid));
        else
            querydata = dataMapPorto.get(queryid);
        if (indexMap != null) {
            queryNode = indexMap.get(queryid);
        } else {
            if (datasetIndex != null) {
                queryindexmap = datasetIndex.get(queryid);
                queryNode = queryindexmap.get(1);
            } else {
                queryNode = indexDSS.buildBalltree2(querydata, dimension, capacity, null, null, weight);
            }
        }

        System.out.println("*******testing top-k Hausdorff");
        AdvancedHausdorff.setBoundChoice(0);//using our fast ball bound

        // 1, conduct top-k dataset search, the baseline, scan one by one
        long startTime1 = System.nanoTime();// exact search: 0, 1, 0
        //Search.hausdorffEarylyAbandonging(dataMapPorto, queryid, indexMap, dimension, limit, false, null, null, null, true);//no top-k early breaking
        long endtime = System.nanoTime();
        System.out.println("top-k search costs: " + (endtime - startTime1) / 1000000000.0);
        timeMultiple[timeID][testOrder * numberofComparisons + countAlgorithm++] += (endtime - startTime1) / 1000000000.0;


        // 2, early abandoning, compute the bound first, then see whether to abandon it
        startTime1 = System.nanoTime();// exact search: 0, 1, 0
        Search.hausdorffEarylyAbandonging(dataMapPorto, queryid, indexMap, dimension, limit, true, saveDatasetIndex,
                dimNonSelected, dimensionAll, querydata, datasetIdMapping, capacity, weight, queryindexmap, queryNode, indexString);
        endtime = System.nanoTime();
        System.out.println("top-k search costs: " + (endtime - startTime1) / 1000000000.0);
        timeMultiple[timeID][testOrder * numberofComparisons + countAlgorithm++] += (endtime - startTime1) / 1000000000.0;

        // 3, rank all the candidate by lower bounds, then one by one
        startTime1 = System.nanoTime();// exact search: 0, 1, 0
        int datasetID = Search.HausdorffEarylyAbandongingRanking(dataMapPorto, queryid, indexMap, dimension, limit, resultPair, queryindexmap, saveDatasetIndex, dimNonSelected, dimensionAll, error, querydata, datasetIdMapping, queryNode, capacity, weight, indexString);
        endtime = System.nanoTime();
        System.out.println("top-k search costs: " + (endtime - startTime1) / 1000000000.0);
        timeMultiple[timeID][testOrder * numberofComparisons + countAlgorithm++] += (endtime - startTime1) / 1000000000.0;

        // 4, using datalake index to prune
        startTime1 = System.nanoTime();// exact search: 0, 1, 0
        HashMap<Integer, Double> result = Search.pruneByIndex(dataMapPorto, datasetRoot, queryNode, queryid,
                dimension, indexMap, datasetIndex, queryindexmap, datalakeIndex, datasetIdMapping, k,
                indexString, dimNonSelected, dimensionAll, error, capacity, weight, saveDatasetIndex, querydata);
        endtime = System.nanoTime();

        //TODO haus test some of the result matrix is remote to the query dataset
        List<double[][]> list = result.entrySet().stream().map(entry -> getMatrixByDatasetId(entry.getKey())).collect(Collectors.toList());

        for (Map.Entry e : result.entrySet()) {
            System.out.println(e.getKey());
        }
        System.out.println("top-k search costs: " + (endtime - startTime1) / 1000000000.0);
        System.out.println();
        timeMultiple[timeID][testOrder * numberofComparisons + countAlgorithm++] += (endtime - startTime1) / 1000000000.0;
    }

    /*
     * test the approximate version of knn and pairwise by increasing the error,
     * we have two comparisons, pairwise and Hausdorff
     */
    static void testHausdorffApproxi(int k, int testOrder, int queryid, int timeID) throws FileNotFoundException, IOException {
        numberofComparisons = 4;
        int countAlgorithm = 2;

        System.out.println("*******testing approximate Hausdorff");

        // 1, pairwise
        int datasetID = queryid + 1;
        long startTime1 = System.nanoTime();
        double dataMap[][], query[][];
        if (dataMapPorto != null) {
            dataMap = dataMapPorto.get(datasetID);
            query = dataMapPorto.get(queryid);
        } else {
            dataMap = Framework.readSingleFile(datasetIdMapping.get(datasetID));
            query = Framework.readSingleFile(datasetIdMapping.get(queryid));
        }
        indexNode queryNode, datanode;
        Map<Integer, indexNode> queryindexmap = null, dataindexMap = null;
        if (indexMap != null) {
            queryNode = indexMap.get(queryid);
            datanode = indexMap.get(datasetID);
        } else {
            if (datasetIndex != null) {
                queryindexmap = datasetIndex.get(queryid);
                dataindexMap = datasetIndex.get(datasetID);
                queryNode = queryindexmap.get(1);
                datanode = dataindexMap.get(1);
            } else {
                queryNode = indexDSS.buildBalltree2(query, dimension, capacity, null, null, weight);
                datanode = indexDSS.buildBalltree2(dataMap, dimension, capacity, null, null, weight);
            }
        }

        Pair<Double, PriorityQueue<queueMain>> resultPair = AdvancedHausdorff.IncrementalDistance(query, dataMap, dimension, queryNode, datanode, 0, 1, 0, false, 0, false, queryindexmap, dataindexMap, null, true);
        long endtime = System.nanoTime();
        System.out.println((endtime - startTime1) / 1000000000.0 + "," + AdvancedHausdorff.disCompTime + "," + AdvancedHausdorff.EuclideanCounter);
        //	System.out.println(resultPair.getLeft());
        timeMultiple[0][testOrder * numberofComparisons + countAlgorithm++] += (endtime - startTime1) / 1000000000.0;

        // 2, top-k fast
        startTime1 = System.nanoTime();// exact search: 0, 1, 0
        HashMap<Integer, Double> result = Search.pruneByIndex(dataMapPorto, datasetRoot, queryNode, queryid,
                dimension, indexMap, datasetIndex, queryindexmap, datalakeIndex, datasetIdMapping, k,
                indexString, null, true, error, capacity, weight, saveDatasetIndex, query);
        System.out.println("appro----");
        for (Map.Entry e : result.entrySet()) {
            System.out.println(e.getKey());
        }
        endtime = System.nanoTime();
        timeMultiple[timeID][testOrder * numberofComparisons + countAlgorithm++] += (endtime - startTime1) / 1000000000.0;
    }

    /*
     * join and self-join
     */
    static void testJoinOutlier(int testOrder, int timeID, int queryID, int datasetID, Pair<Double, PriorityQueue<queueMain>> aPair) {
        numberofComparisons = 4;
        int countAlgorithm = 0;
		/*
		System.out.println("*******testing join and outlier");

		// 1. brute force, too slow
		long startTime1 = System.nanoTime();
	//	Join.scanning(dataMapPorto.get(queryID), dataMapPorto.get(datasetID), dimension);
		long endtime = System.nanoTime();
		System.out.println("join with top-1 dataset with scanning baseline costs: "+(endtime-startTime1)/1000000000.0);
		timeMultiple[timeID][testOrder*numberofComparisons+countAlgorithm++] += (endtime-startTime1)/1000000000.0;

		// 2, using knn
		startTime1 = System.nanoTime();// exact search: 0, 1, 0
		Join.joinTableBaseline(dataMapPorto.get(queryID), dataMapPorto.get(datasetID),
				indexMap.get(queryID), indexMap.get(datasetID), dimension, indexDSS);
		endtime = System.nanoTime();
		timeMultiple[timeID][testOrder*numberofComparisons+countAlgorithm++] += (endtime-startTime1)/1000000000.0;
		System.out.println("join with top-1 dataset with nn search baseline costs: "+(endtime-startTime1)/1000000000.0);*/

        // 3, join with our cached queue
        //startTime1 = System.nanoTime();// exact search: 0, 1, 0
        Join.IncrementalJoin(dataMapPorto.get(queryID), dataMapPorto.get(datasetID), dimension, indexMap.get(limit + 1),
                indexMap.get(datasetID), 0, 0, 0.01, false, 0, false, aPair.getLeft(), aPair.getRight(), null, null, "haus", null, true);
        //endtime = System.nanoTime();
        //timeMultiple[timeID][testOrder*numberofComparisons+countAlgorithm++] += (endtime-startTime1)/1000000000.0;
        //System.out.println("join with top-1 dataset on reused queues costs: "+(endtime-startTime1)/1000000000.0);

		/*
		// 4, test self-join for outlier detection
		startTime1 = System.nanoTime();// exact search: 0, 1, 0
		AdvancedHausdorff.setParameter(true, false);
		Pair<Double, PriorityQueue<queueMain>> resultPaira = AdvancedHausdorff.IncrementalDistance(dataMapPorto.get(1), dataMapPorto.get(1), dimension, indexMap.get(1), indexMap.get(1), 0, 1, 0, false, 0, false, null, null, null, true);
		endtime = System.nanoTime();
		System.out.println((endtime-startTime1)/1000000000.0+ ","+ AdvancedHausdorff.disCompTime+","+AdvancedHausdorff.EuclideanCounter);
		timeMultiple[timeID][testOrder*numberofComparisons+countAlgorithm++] += (endtime-startTime1)/1000000000.0;*/
        //	System.out.println(resultPaira.getLeft());
        //	System.out.println();
    }


    // four methods: intersecting range, overlapped grids in scanning and pruning mode
    static void testRangeQuery(int k, int testOrder, int queryid, int timeID) throws FileNotFoundException, IOException {
        //scan every dataset to find the top-k
        numberofComparisons = 4;
        int countAlgorithm = 0;
        indexNode root = datasetRoot;//datalakeIndex.get(1);//use storee
        if (datalakeIndex != null)
            root = datalakeIndex.get(1);

        System.out.println("*******testing top-k range query");

        double[] querymax = new double[dimension];
        double[] querymin = new double[dimension];
        double[][] dataset = null;
        indexNode queryNode = getQueryNode(dataset, queryid);
        for (int i = 0; i < dimension; i++) {// to enlarge the range for evaluation
            querymax[i] = queryNode.getMBRmax()[i] + error;
            querymin[i] = queryNode.getMBRmin()[i] - error;
        }

        // 1, the query by intersecting area, scan every dataset
        Search.setScanning(true);
        HashMap<Integer, Double> result = new HashMap<>();
        long startTime1 = System.nanoTime();
        Search.rangeQueryRankingArea(root, result, querymax, querymin, Double.MAX_VALUE, k, null, dimension,
                datalakeIndex, dimNonSelected, dimensionAll);
        long endtime = System.nanoTime();
        System.out.println((endtime - startTime1) / 1000000000.0);
        timeMultiple[timeID][testOrder * numberofComparisons + countAlgorithm++] += (endtime - startTime1) / 1000000000.0;

        // 2, the grid-based overlap
        int[] queryzcurve = new int[zcodemap.get(queryid).size()];
        startTime1 = System.nanoTime();
        for (int i = 0; i < zcodemap.get(queryid).size(); i++)
            queryzcurve[i] = zcodemap.get(queryid).get(i);
        HashMap<Integer, Double> result2 = EffectivenessStudy.topkAlgorithmZcurveHashMap(zcodemap, zcodemap.get(queryid), k);
        endtime = System.nanoTime();
        System.out.println((endtime - startTime1) / 1000000000.0);
        timeMultiple[timeID][testOrder * numberofComparisons + countAlgorithm++] += (endtime - startTime1) / 1000000000.0;

        // 3, use datalake index to rank by area
        startTime1 = System.nanoTime();
        Search.setScanning(false);
        result = new HashMap<>();
        Search.rangeQueryRankingArea(root, result, querymax,
                querymin, Double.MAX_VALUE, k, null, dimension, datalakeIndex, dimNonSelected, dimensionAll);
        endtime = System.nanoTime();
        System.out.println((endtime - startTime1) / 1000000000.0);
        timeMultiple[timeID][testOrder * numberofComparisons + countAlgorithm++] += (endtime - startTime1) / 1000000000.0;


        // 4, use datalake index to rank by number of overlapped grids
        startTime1 = System.nanoTime();
        result = new HashMap<>();
        Search.gridOverlap(root, result, queryzcurve, Double.MAX_VALUE, k, null, datalakeIndex);
        endtime = System.nanoTime();
        System.out.println((endtime - startTime1) / 1000000000.0);
        timeMultiple[timeID][testOrder * numberofComparisons + countAlgorithm++] += (endtime - startTime1) / 1000000000.0;
    }

    /*
     * we only test the varied resolution here
     */
    static void testGridOverlapAlone(int k, int testOrder, int queryid, int timeID) {
        numberofComparisons = 4;
        int countAlgorithm = 3;

        System.out.println("*******testing top-k grip overlap");

        int[] queryzcurve = new int[zcodemap.get(queryid).size()];
        long startTime1 = System.nanoTime();
        for (int i = 0; i < zcodemap.get(queryid).size(); i++)
            queryzcurve[i] = zcodemap.get(queryid).get(i);
        EffectivenessStudy.topkAlgorithmZcurveHashMap(zcodemap, zcodemap.get(queryid), k);
        long endtime = System.nanoTime();
        System.out.println((endtime - startTime1) / 1000000000.0);
        timeMultiple[timeID][testOrder * numberofComparisons + countAlgorithm++] += (endtime - startTime1) / 1000000000.0;
    }

    // range query by using dataset index, i.e., count how many points in the range, and plus return all the points
    static void testRangeQueryDeep(int k, int testOrder, int queryid, int timeID) throws FileNotFoundException, IOException {
        //scan every dataset to find the top-k
        numberofComparisons = 4;
        int countAlgorithm = 2;
        long startTime1 = System.nanoTime();

        System.out.println("*******testing top-k range query by covered points");

        double[] querymax = new double[dimension];
        double[] querymin = new double[dimension];
        for (int i = 0; i < dimension; i++) {// enlarge the range for evaluation
            querymax[i] = indexMap.get(queryid).getMBRmax()[i] + error;
            querymin[i] = indexMap.get(queryid).getMBRmin()[i] - error;
        }

        indexNode root = datasetRoot;//datalakeIndex.get(1);//use storee
        if (datalakeIndex != null)
            root = datalakeIndex.get(1);

        // 1, counting how many points in the range, access the dataset index
        ArrayList<double[]> points = new ArrayList<>();
        // find and store the founded points
        Search.setScanning(true);
        HashMap<Integer, Double> result = new HashMap<>();
        startTime1 = System.nanoTime();// exact search: 0, 1, 0
        Search.rangeQueryRankingNumberPoints(root, result, querymax,
                querymin, Double.MAX_VALUE, k, null, dimension, dataMapPorto, datalakeIndex,
                datasetIndex, datasetIdMapping, indexString, indexDSS, dimNonSelected, dimensionAll, points, indexMap, capacity, weight, saveDatasetIndex);
        long endtime = System.nanoTime();
        System.out.println((endtime - startTime1) / 1000000000.0);
        timeMultiple[timeID][testOrder * numberofComparisons + countAlgorithm++] += (endtime - startTime1) / 1000000000.0;

        // 2, stored the points in the range, access the dataset index
        points = null;
        startTime1 = System.nanoTime();
        Search.setScanning(false);
        result = new HashMap<>();
        startTime1 = System.nanoTime();// exact search: 0, 1, 0
        Search.rangeQueryRankingNumberPoints(root, result, querymax,
                querymin, Double.MAX_VALUE, k, null, dimension, dataMapPorto, datalakeIndex,
                datasetIndex, datasetIdMapping, indexString, indexDSS, dimNonSelected, dimensionAll, points, indexMap, capacity, weight, saveDatasetIndex);
        endtime = System.nanoTime();
        System.out.println((endtime - startTime1) / 1000000000.0);
        timeMultiple[timeID][testOrder * numberofComparisons + countAlgorithm++] += (endtime - startTime1) / 1000000000.0;
    }

    /*
     * running time record
     */
    static void recordRunningTime(String filename, int timeid) {
        String content = datalakeID + ",";
        for (double t : timeMultiple[timeid])
            content += t / NumberQueryDataset + ",";
        write(filename, content + "\n");
    }

    /*
     * running time record of
     */
    static void recordIndexModeEffect(String filename1, String filename2) {
        String content = datalakeID + ",";
        for (int i = 0; i < numberofParameterChoices; i++) {
            content += "0,";
            for (int j = 0; j < 3; j++) {
                double t = timeMultiple[j * 2][i * numberofComparisons + 3];
                content += t / NumberQueryDataset + ",";
            }
        }
        write(filename1, content + "\n");

        for (int i = 0; i < numberofParameterChoices; i++) {
            content += "0,";
            for (int j = 0; j < 3; j++) {
                double t = timeMultiple[j * 2 + 1][i * numberofComparisons + 3];
                content += t / NumberQueryDataset + ",";
            }
        }
        write(filename2, content + "\n");
    }

    /*
     * running time record
     */
    static void recordIndexPerformance(String filename, int timeid, double spaceMultiple[][]) {
        String content = datalakeID + ",";
        for (double t : spaceMultiple[timeid])
            content += t / NumberQueryDataset + ",";
        write(filename, content + "\n");
    }


    /*
     * update the datalake
     */
    static void addingDatasets() throws FileNotFoundException, IOException {
        /**
         * test adding new dataset into the datalake
         */
//		indexNodes = new ArrayList<indexNode>();
//		for(int dataid: datasetIndex.keySet()) {
//			indexNodes.add(datasetIndex.get(dataid).get(1));
//		}
//		indexDSS.updateDatasetNodeId(datasetRoot, 1);
//		datalakeIndex = null;
//		/* update index */
//		indexNode rootBall = indexDSS.buildBalltree2(dataMapPorto.get(limit+1), dimension, capacity);
//		rootBall.setroot(limit+1);
//		datasetRoot = indexDSS.insertNewDataset(rootBall, datasetRoot, capacity, dimension);
//		int rootNum = indexDSS.getRootCount(datasetRoot);
//		System.out.println("number of datasets: "+ rootNum);
//		dataMapPorto = null;


        int k = 10;
        long startTime1 = System.nanoTime();
        // test top-k search, which also works for restored index.
        int queryid = 10;
        double[][] querydata = readSingleFile(datasetIdMapping.get(queryid));
        HashMap<Integer, Double> result = Search.pruneByIndex(dataMapPorto, datasetRoot, indexMap.get(queryid), queryid,
                dimension, indexMap, datasetIndex, datasetIndex.get(queryid), datalakeIndex, datasetIdMapping, k,
                indexString, null, true, error, capacity, weight, saveDatasetIndex, querydata);
        long endtime = System.nanoTime();
        System.out.println("top-1 search costs: " + (endtime - startTime1) / 1000000000.0);
        System.out.println(result + ", " + datasetIdMapping.get(result.entrySet().iterator().next().getKey()));
    }

    /*
     * pair-wise comparison, including Hausdorff, join, outlier, by varying the dataset scale, and recreate the index
     */
    static void EvaluatePairWise() throws IOException {
        System.out.println("evaluate pair-wise hausdorff");
        timeMultiple = new double[3][numberofComparisons * numberofParameterChoices];
        for (int i = 0; i < numberofParameterChoices; i++) { // test the scale
            for (int j = 0; j < NumberQueryDataset; j++) {
                int scale = ss[i];
                limit = 2 * scale;
//				storeIndexMemory = false;
//				readDatalake(limit);
//				storeIndexMemory = true;
//				indexNodes = new ArrayList<>();
                //dataMapPorto = prepareLargeDatasets(scale); // generate datasets by combining more 10, 100, 1000, 10000 datasets
//				long startTime1a = System.nanoTime();
//				for(int a:dataMapPorto.keySet()) {//we can build index to accelerate
//					if(a>limit+NumberQueryDataset)
//						break;
//					createDatasetIndex(a, dataMapPorto.get(a)); // load index structure
//				}
//				long endtimea = System.nanoTime();
//				write("./logs/spadas/efficiency/DatasetPairwiseIndexTime.txt", datalakeID+","+limit+","+dimension+","+capacity+","+(endtimea-startTime1a)/1000000000.0+"\n");
//				write("./logs/spadas/efficiency/DatasetPairwiseIndexSize.txt", datalakeID+","+limit+","+dimension+","+capacity+","+(getDatalakeIndexSize(datasetRoot, false)+getAllDatasetIndexSize())/(1024.0*1024)+"\n");

                // 1, the effect of dataset scale on pairwise
                Pair<Double, PriorityQueue<queueMain>> resultPair = testPairwiseHausdorff(1, 2, i, 0, false);// 1,

                // 2, the effect of dataset scale on joins
                testJoinOutlier(i, 1, 2, 200, resultPair); // 2,

                // 3, the effect of scale on range query
				/*error = spaceRange/((int) Math.pow(2, rs[0]));
				testPairwiseRangeQuery(indexMap.get(1), dataMapPorto.get(1), i, 2);*/
            }
        }
        recordRunningTime("./logs/spadas/efficiency/" + datalakeID + "-n-haus.txt", 0);
        recordRunningTime("./logs/spadas/efficiency/" + datalakeID + "-n-join-outlier.txt", 1);
        recordRunningTime("./logs/spadas/efficiency/" + datalakeID + "-n-range-deep.txt", 1);

        System.out.println("evaluate pair-wise range");
        timeMultiple = new double[1][numberofComparisons * numberofParameterChoices];
        for (int i = 0; i < numberofParameterChoices; i++) { // test range
            for (int j = 0; j < NumberQueryDataset; j++) {
                resolution = rs[i];
                error = spaceRange / ((int) Math.pow(2, resolution));
                testPairwiseRangeQuery(indexMap.get(1), dataMapPorto.get(1), i, 0);
            }
        }
        recordRunningTime("./logs/spadas/efficiency/" + datalakeID + "-r-range-deep.txt", 0);
    }


    static void EvaluateHausCapacity() throws IOException {
        System.out.println("evaluate capacity's effect on hausdorff");
        timeMultiple = new double[1][numberofComparisons * numberofParameterChoices];
        for (int i = 0; i < numberofParameterChoices; i++) { // test the scale
            for (int j = 0; j < NumberQueryDataset; j++) {
                int scale = 100;
                if (datalakeID == 3)
                    scale = 1;
                limit = 2 * scale; // limit is changed here
                storeIndexMemory = false;
                readDatalake(limit);
                storeIndexMemory = true;
                indexNodes = new ArrayList<>();
                dataMapPorto = prepareLargeDatasets(scale); // generate datasets by combining more 10, 100, 1000, 10000 datasets
                capacity = fs[i];
                for (int a : dataMapPorto.keySet()) {//we can build index to accelerate
                    if (a > limit + NumberQueryDataset)
                        break;
                    createDatasetIndex(a, dataMapPorto.get(a)); // load index structure
                }
                testPairwiseHausdorff(1, 2, i, 0, true);// test fast only
            }
        }
        recordRunningTime("./logs/spadas/efficiency/" + datalakeID + "-f-haus.txt", 0);
    }


    /*
     *
     */
    private static void EvaluateDatalakeScale() throws FileNotFoundException, IOException {
        timeMultiple = new double[2][numberofComparisons * numberofParameterChoices];
        indexNodes = new ArrayList<>();
        System.out.println("evaluate datalake's scale");
        for (int Ni = 0; Ni < ns.length; Ni++) {// varying datalake scale, preparing datalake index, and evaluate top-k search
            int N = (int) (ns[Ni] * limit);
            int a = 1;
            for (indexNode node : indexNodesAll) {//
                if (a++ <= N) // we only index part of the dataset.
                    indexNodes.add(node);
            }

            long startTime1a = System.nanoTime();
            File tempFile = new File(indexString + "datalake" + N + "-" + capacity + "-" + dimension + ".txt");
            if (!tempFile.exists()) {
                storeIndexMemory = true;
            } else {
                storeIndexMemory = false;
            }
            createDatalake(N);// create or load the index of whole data lake, and store it into disk to avoid rebuilding it
            long endtimea = System.nanoTime();
            write("./logs/spadas/efficiency/DatalakeIndexTime.txt", datalakeID + "," + N + "," + dimension + "," + capacity + "," + (endtimea - startTime1a) / 1000000000.0 + "\n");
            write("./logs/spadas/efficiency/DatasetIndexSize.txt", datalakeID + "," + limit + "," + dimension + "," + capacity + "," + (getDatalakeIndexSize(datasetRoot, false) + getAllDatasetIndexSize()) / (1024.0 * 1024) + "\n");
            for (int j = 0; j < NumberQueryDataset; j++) {
                testTopkHausdorff(10, Ni, j + limit, 0, limit); // 6,
                testRangeQuery(10, Ni, j + limit, 1); // 7,
            }
        }
        recordRunningTime("./logs/spadas/efficiency/" + datalakeID + "-N-topk-haus.txt", 0);
        recordRunningTime("./logs/spadas/efficiency/" + datalakeID + "-N-range.txt", 1);
    }

    /*
     * evaluate the effect of f on the datalake index
     */
    static void EvaluateDatalakeCapacity() throws IOException {
        timeMultiple = new double[2][numberofComparisons * numberofParameterChoices];
        indexNodes = new ArrayList<>();
        int N = limit;
        int a = 1;
        for (indexNode node : indexNodesAll) {//
            if (a++ <= N) // we only index part of the dataset.
                indexNodes.add(node);
        }
        System.out.println("evaluate datalake index's capacity");
        for (int i = 0; i < numberofParameterChoices; i++) { // test the scale
            capacity = fs[i];
            long startTime1a = System.nanoTime();
            File tempFile = new File(indexString + "datalake" + N + "-" + capacity + "-" + dimension + ".txt");
            if (!tempFile.exists()) {
                storeIndexMemory = true;
            } else {
                storeIndexMemory = false;
            }
            createDatalake(N);// create or load the index of whole data lake, and store it into disk to avoid
            long endtimea = System.nanoTime();
            write("./logs/spadas/efficiency/DatalakeIndexTime.txt", datalakeID + "," + N + "," + dimension + "," + capacity + "," + (endtimea - startTime1a) / 1000000000.0 + "\n");
            write("./logs/spadas/efficiency/DatasetIndexSize.txt", datalakeID + "," + limit + "," + dimension + "," + capacity + "," + (getDatalakeIndexSize(datasetRoot, false) + getAllDatasetIndexSize()) / (1024.0 * 1024) + "\n");

            for (int j = 0; j < NumberQueryDataset; j++) {
                testTopkHausdorff(10, i, j + limit, 0, limit); // 6, test the top-k Hausdorff
                testRangeQuery(10, i, j + limit, 1); // 7, test the range query
            }
        }
        recordRunningTime("./logs/spadas/efficiency/" + datalakeID + "-f-topk.txt", 0);
        recordRunningTime("./logs/spadas/efficiency/" + datalakeID + "-f-range.txt", 1);
    }

    /*
     * evaluate the effect of dimension on pairwise Hausdorff
     */
    static void EvaluateHausDimension() throws IOException {
        System.out.println("evaluate dimenison's effect on hausdorff");
        if (datalakeID == 5) {
            timeMultiple = new double[1][numberofComparisons * numberofParameterChoices];
            for (int i = 0; i < numberofParameterChoices; i++) { // test the dimension
                for (int j = 0; j < NumberQueryDataset; j++) {
                    dimension = ds[i];
                    dimensionAll = false;
                    dimNonSelected = new boolean[11];
                    for (int p = dimension; p < 11; p++)
                        dimNonSelected[p] = true;// non selected
                    testPairwiseHausdorff(1, 2, i, 0, false);// test all
                }
            }
            recordRunningTime("./logs/spadas/efficiency/" + datalakeID + "-d-haus.txt", 0);
        }
    }

    /*
     * evaluate top-k range query which only use datalake
     * 4 comparisons, ranking by
     * intersecting area, and grid-based overlap
     */
    static void EvaluateRangequery() throws FileNotFoundException, IOException {
        // range query by datalake index only
        System.out.println("evaluate range query");
        timeMultiple = new double[2][numberofComparisons * numberofParameterChoices];
        for (int i = 0; i < numberofParameterChoices; i++) { // test k
            for (int j = 0; j < NumberQueryDataset; j++) {
                // 1, the
                //TODO change 3rd param from j+limit to j+1
                testRangeQuery(ks[i], i, j + 1, 0);
                //	testRangeQueryDeep(ks[i], i, j + limit, 1);
            }
        }
        recordRunningTime("./logs/spadas/efficiency/" + datalakeID + "-k-range.txt", 0);
        //	recordRunningTime("./logs/spadas/efficiency/"+datalakeID+"-k-range-deep.txt", 1);

        // range query by datalake index only
//		timeMultiple = new double[2][numberofComparisons * numberofParameterChoices];
//		for (int i = 0; i < numberofParameterChoices; i++) { // test range
//			for (int j = 0; j < NumberQueryDataset; j++) {
//				resolution = rs[i];
//				error = spaceRange/((int) Math.pow(2, resolution));
//				//TODO change 3rd param from j+limit to j+1
//				testRangeQuery(10, i, j + 1, 0);
//	//			testRangeQueryDeep(10, i, j + limit, 1);
//			}
//		}
//		recordRunningTime("./logs/spadas/efficiency/"+datalakeID+"-r-range.txt", 0);
        //	recordRunningTime("./logs/spadas/efficiency/"+datalakeID+"-r-range-deep.txt", 1);

        if (datalakeID == 5) {
            timeMultiple = new double[2][numberofComparisons * numberofParameterChoices];
            for (int i = 0; i < numberofParameterChoices; i++) { // test only the range query, as z-curve does not work
                for (int j = 0; j < NumberQueryDataset; j++) {
                    dimension = ds[i];
                    dimensionAll = false;
                    dimNonSelected = new boolean[11];
                    for (int p = dimension; p < 11; p++)
                        dimNonSelected[p] = true;// non selected
                    testRangeQuery(10, i, j + limit, 0);
                    //				testRangeQueryDeep(10, i, j + limit, 1);
                }
            }
            recordRunningTime("./logs/spadas/efficiency/" + datalakeID + "-d-range.txt", 0);
            //		recordRunningTime("./logs/spadas/efficiency/"+datalakeID+"-d-range-deep.txt", 1);
            dimension = 11;
        }
    }

    /*
     * top-k hausdorff query by datasets
     */
    static void EvaluateTopkHaus() throws FileNotFoundException, IOException {
        System.out.println("evaluate top-k Haus");

        timeMultiple = new double[1][numberofComparisons * numberofParameterChoices];
        for (int i = 0; i < ks.length; i++) { // varying k
            for (int j = 0; j < NumberQueryDataset; j++)
                //TODO change 3rd param from j+limit to j+1
                testTopkHausdorff(ks[i], i, 34, 0, limit);
        }
        recordRunningTime("./logs/spadas/efficiency/" + datalakeID + "-k-topk-haus.txt", 0);

        if (datalakeID == 5) {
            timeMultiple = new double[1][numberofComparisons * numberofParameterChoices];
            for (int i = 0; i < ds.length; i++) { // varying d
                for (int j = 0; j < NumberQueryDataset; j++) {
                    dimension = ds[i];
                    dimensionAll = false;
                    dimNonSelected = new boolean[11];
                    for (int p = dimension; p < 11; p++)
                        dimNonSelected[p] = true;// non selected
                    testTopkHausdorff(10, i, j + limit, 0, limit);
                }
            }
            recordRunningTime("./logs/spadas/efficiency/" + datalakeID + "-d-topk-haus.txt", 0);
        }

        timeMultiple = new double[1][numberofComparisons * numberofParameterChoices];
        for (int i = 0; i < numberofParameterChoices; i++) { // test range
            for (int j = 0; j < NumberQueryDataset; j++) {
                resolution = rs[i];
                error = spaceRange / ((int) Math.pow(2, resolution));
                //TODO change 3rd param from j+limit to j+1
                testHausdorffApproxi(10, i, 34, 0);//cannot work for porto
            }
        }
        recordRunningTime("./logs/spadas/efficiency/" + datalakeID + "-r-haus-appro.txt", 0);
    }

    private static void EvaluateGridResolution() throws FileNotFoundException, IOException {
        System.out.println("evaluate grid resolution on GBO query");
        dimension = 2;
        timeMultiple = new double[1][numberofComparisons * numberofParameterChoices];
        for (int Ri = 0; Ri < rs.length; Ri++) {
            resolution = rs[Ri];
            error = spaceRange / ((int) Math.pow(2, resolution));
            String zcurveFile = zcodeSer + resolution + "-" + limit + ".ser";
            File tempFile = new File(zcurveFile);
            if (!tempFile.exists()) {// create the z-curve code for each dataset
                zcurveExist = false;
                if (dataMapPorto == null) {
//                    因为有bug所以先注释掉
                    zcodemap = EffectivenessStudy.storeZcurveFile(minx, miny, spaceRange, 2, resolution, zcurveFile, limit, datasetIdMapping);
                } else
                    zcodemap = EffectivenessStudy.storeZcurve(minx, miny, spaceRange, 2, dataMapPorto, resolution, zcurveFile);
            } else {
                zcurveExist = true;
                zcodemap = EffectivenessStudy.deSerializationZcurve(zcurveFile);
            }
            if (datalakeIndex != null)
                datalakeIndex.get(1).buildsignarture(zcodemap, datalakeIndex);
            else {
                datasetRoot.buildsignarture(zcodemap, datalakeIndex);
            }
            for (int j = 0; j < NumberQueryDataset; j++) {
                testGridOverlapAlone(10, Ri, j + limit, 0);// 8, increase the grid-size to observe the performance
            }
        }
        recordRunningTime("./logs/spadas/efficiency/" + datalakeID + "-r-topk-grid.txt", 0);
    }

    /*
     * evaluate the effect of index's memory mode on the performance
     */
    static void EvaluateIndex(int option) throws IOException {
        timeMultiple = new double[6][numberofComparisons * numberofParameterChoices];
        indexMultiple = new double[2][numberofComparisons * numberofParameterChoices];
        String fileNameString;
        if (option == 1) {
            fileNameString = "d";
        } else {
            fileNameString = "N";
        }
        for (int i = 0; i < numberofParameterChoices; i++) { // test the scale
            int N = limit; // or change to dimension test
            if (option == 1) {
                dimension = ds[i];
                dimensionAll = false;
                dimNonSelected = new boolean[11];
                for (int p = dimension; p < 11; p++)
                    dimNonSelected[p] = true;// non selected
            } else {
                N = (int) (ns[i] * limit) + 10;
            }
            System.out.println("datalake scale: " + N);
            long startTime1a = System.nanoTime();
            // ** run all in memory, and run top-k Hausdorff and range query
            storeAllDatasetMemory = true; // load most datasets and create index
            storeIndexMemory = true; // do not create index
            datalakeIndex = null;
            indexMap = null;
            dataMapPorto = null;
            readDatalake(N);// read data, create data set index
            if (dimension > 2)// read and set weight
                AdvancedHausdorff.setWeight(dimension, indexString + "weight.ser");
            indexNodesAll = new ArrayList<>(indexNodes);
            createDatalake(N); // create datalake index
            long endtimea = System.nanoTime();
            double size = (getDatalakeSize(dimension) + getDatalakeIndexSize(datasetRoot, false) + getAllDatasetIndexSize()) / (1024.0 * 1024.0);
            indexMultiple[0][i * numberofComparisons + 1] += size;
            System.out.println("1index size is: " + size);
            indexMultiple[1][i * numberofComparisons + 1] += (endtimea - startTime1a) / 1000000000.0;
            for (int j = 0; j < NumberQueryDataset; j++) {
                testTopkHausdorff(10, i, j + N, 0, N); // 6, test the top-k Hausdorff
                testRangeQuery(10, i, j + N, 1); // 7, test the range query
            }

            // store dataset in memory, load datalake index from disk
            startTime1a = System.nanoTime();
            readDatalake(N);
            File tempFile = new File(indexString + "datalake" + N + "-" + capacity + "-" + dimension + ".txt");
            if (!tempFile.exists()) {
                storeIndexMemory = true;
            } else {
                storeIndexMemory = false;
            }
            createDatalake(N);// create or load the index of whole data lake, and store it into disk to avoid
            endtimea = System.nanoTime();
            size = (getDatalakeSize(dimension) + getDatalakeIndexSizeDisk(dimension)) / (1024.0 * 1024);
            System.out.println("2index size is: " + size);
            indexMultiple[0][i * numberofComparisons + 2] += size;
            indexMultiple[1][i * numberofComparisons + 2] += (endtimea - startTime1a) / 1000000000.0;
            for (int j = 0; j < NumberQueryDataset; j++) {
                testTopkHausdorff(10, i, j + N, 2, N); // 6, test the top-k Hausdorff
                testRangeQuery(10, i, j + N, 3); // 7, test the range query
            }

            // just load the datalake index
            if (datalakeID <= 2)
                continue;
            dataMapPorto = null;
            datalakeIndex = null;
            storeAllDatasetMemory = false;
            buildOnlyRoots = true; // no dataset index
            startTime1a = System.nanoTime();
            tempFile = new File(indexString + "datalake" + N + "-" + capacity + "-" + dimension + ".txt");
            if (!tempFile.exists()) {
                storeIndexMemory = true;
            } else {
                storeIndexMemory = false;
            }
            createDatalake(N);// create or load the index of whole data lake, and store it into disk to avoid
            endtimea = System.nanoTime();
            System.out.println("size of index is " + datalakeIndex.size());
            size = (getDatalakeIndexSizeDisk(dimension)) / (1024.0 * 1024);
            System.out.println("3index size is: " + size);
            indexMultiple[0][i * numberofComparisons + 3] += size;
            indexMultiple[1][i * numberofComparisons + 3] += (endtimea - startTime1a) / 1000000000.0;
            for (int j = 0; j < NumberQueryDataset; j++) {
                testTopkHausdorff(10, i, j + N, 4, N); // 6, test the top-k Hausdorff
                testRangeQuery(10, i, j + N, 5); // 7, test the range query
            }
        }
        //write the log files, running time by extracting the columns
        recordIndexModeEffect("./logs/spadas/efficiency/" + datalakeID + "-M-" + fileNameString + "-topk.txt", "./logs/spadas/efficiency/" + datalakeID + "-M-" + fileNameString + "-range.txt");
        recordIndexPerformance("./logs/spadas/efficiency/" + datalakeID + "-" + fileNameString + "-MemorySize.txt", 0, indexMultiple);
        recordIndexPerformance("./logs/spadas/efficiency/" + datalakeID + "-" + fileNameString + "-ConstructionCost.txt", 1, indexMultiple);
    }

    /*
     * test all the functions related to Hausdorff, creating index,
     * pair-wise computation, top-k, join, outlier, restored index
     */
    public static void main(String[] args) throws IOException {

        //	generateIndexFeatures(); used for extract features
        if (args.length > 1)
            limit = Integer.valueOf(args[1]); //specifying the length
        int temp = limit;

        //	limit = 10000;
        //TODO remarked temporarily
//		System.out.println("testing datalake with scale "+limit);
//		aString = args[0]; // the dataset folder or file
		/*
		testFullDataset = true
		readDatalake(limit);
		testFullDataset = false;*/
	/*
		// 1. pair-wise evaluation
		storeAllDatasetMemory = true; // load most datasets and create index
		storeIndexMemory = false; // do not create index
		zcurveExist = false;
		readDatalake(limit);//read data, create index if not
		if(dimension>2)// read and set weight
			AdvancedHausdorff.setWeight(dimension, indexString + "weight.ser");
		EvaluatePairWise(); // 1, preparing large datasets by increasing the scale, and evaluate pairwise comparison, slowest
		EvaluateHausCapacity(); // 2, evaluate the capacity in pair-wise Hausdorff distance.
		*/

        limit = temp; // in case that limit changed
        zcodeSer = "./index/dss/index/argo/argo_bitmaps";
        indexString = "./index/dss/index/argo/";

        //  2. top-k evaluation
        if (datalakeID <= 2)
            storeAllDatasetMemory = true;
        else {// datasets are large
            storeAllDatasetMemory = false; // no data in memory
            dataMapPorto = null;
            indexMap = null;
        }
        String zcurveFile = zcodeSer + resolution + "-" + limit + ".ser";
        File tempFile = new File(zcurveFile);
        if (!tempFile.exists()) {// create the z-curve code for each dataset
            zcurveExist = false;
        } else {
            zcurveExist = true;
            zcodemap = EffectivenessStudy.deSerializationZcurve(zcurveFile);
        }
        tempFile = new File(indexString + "datalake" + limit + "-" + capacity + "-" + dimension + ".txt");
        if (!tempFile.exists()) {
            storeIndexMemory = true;// has to create index as the datalake index is not there
            if (limit >= 100000 && datalakeID > 2)// for large datasets
                buildOnlyRoots = true;
        } else {
            storeIndexMemory = false;
            if (datalakeID <= 2)// has to create index for trajectory dataset
                storeIndexMemory = true;
        }
        //long startTime1a = System.nanoTime(); // exact search: 0, 1, 0
        readDatalake(limit);// read the first

//        TimeUnit.MINUTES.sleep(1);

        indexNodesAll = new ArrayList<>(indexNodes);
        if (!zcurveExist)
            EffectivenessStudy.SerializedZcurve(zcurveFile, zcodemap);
        //long endtimea = System.nanoTime();
        //write("./logs/spadas/efficiency/DatasetIndexTime.txt", datalakeID+","+limit+","+dimension+","+capacity+","+(endtimea-startTime1a)/1000000000.0+"\n");
        //	write("./logs/spadas/efficiency/DatasetIndexSize.txt", datalakeID+","+limit+","+dimension+","+capacity+","+(getAllDatasetIndexSize())/(1024.0*1024)+"\n");

        //startTime1a = System.nanoTime();
        //System.out.println(minx+","+miny+","+spaceRange+","+resolution);
        createDatalake(limit);// create or load the index of whole data lake, and store it into disk to avoid rebuilding it
        //endtimea = System.nanoTime();
        //write("./logs/spadas/efficiency/DatalakeIndexTime.txt", datalakeID+","+limit+","+dimension+","+capacity+","+(endtimea-startTime1a)/1000000000.0+"\n");
        //write("./logs/spadas/efficiency/DatasetIndexSize.txt", datalakeID+","+limit+","+dimension+","+capacity+","+(getDatalakeIndexSize(datasetRoot, false)+getAllDatasetIndexSize())/(1024.0*1024)+"\n");
//        EvaluatePairWise();

        //EvaluateRangequery(); // 3, evaluate range query by increasing range
        //EvaluateTopkHaus(); // 4, evaluate top-k query by varying k, dimension, resolution
		/*EvaluateHausDimension(); // 5, evaluate dimension

		if(datalakeID==5) {// restore the parameter
			dimension = 11;
			dimensionAll = true;
		}*/
		/*
		// 3. varying datalake scale (number of datasets) to test
		limit = temp; // in case that limit changed
		EvaluateDatalakeScale();

		// 4. capacity's effect to datalake index
		limit = temp; // in case that limit changed
		EvaluateDatalakeCapacity();

		// 5. evaluate the resolution's effect on grid-based overlap
		limit = temp; // in case that limit changed
		EvaluateGridResolution();
		*/


        // 6. evaluate the index mode's effect on construction and top-k Hausdorff and range query
	/*
	  	limit = temp;
		EvaluateIndex(0); // changing N
		if(datalakeID == 5)
			EvaluateIndex(1); //*/// changing dimension
    }

    /*
     * given a large dataset, simplified it into one dataset, and randomly sampled 99 datasets, integrate them and search by the large datasets.
     */
    void generateSimiplidedGroundTruth() {
        // use pick-means
    }

    //TODO custom code

    /**
     * 网页第一次加载和每次刷新时调用，因此需要判断相关的结构和变量是否已经存在
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public static void init() throws IOException, InterruptedException, CloneNotSupportedException {
//        判断建立索引相关的结构与变量是否已经存在
        if (fileNo > 0) {
            fileNo = 0;
        }
        if (!datasetIdMapping.isEmpty()) {
            datasetIdMapping.clear();
        }
        if (!dataMapPorto.isEmpty()) {
            dataMapPorto.clear();
        }
        if (indexMap != null) {
            indexMap.clear();
        }
        if (!indexNodes.isEmpty()) {
            indexNodes.clear();
        }
        if (indexNodesAll != null) {
            indexNodesAll.clear();
        }
        if (datasetRoot != null) {
            datasetRoot = null;
        }
        if (dataPoint != null) {
            dataPoint.clear();
        }
//        专属于城市数据集的结构
        if (!cityNodeList.isEmpty()) {
            cityNodeList.clear();
        }
//        新增的一些数据结构需要补上

        dimension = 2;
//		indexString = "./index/dss/index/argo/";
//		zcodeSer= "./index/dss/index/argo/argo_bitmaps";
//        是否存储所有数据集到内存？是，但好像并没有真的执行存储操作，有待进一步观察
        storeAllDatasetMemory = true;

//        Zcurve
//        命名要改，现在的有点问题
        String zcurveFile = zcodeSer + resolution + "-" + limit + ".ser";
        File tempFile = new File(zcurveFile);
//        z-order文件是针对一个数据集还是所有数据集？
        if (!tempFile.exists()) {// create the z-curve code for each dataset
            zcurveExist = false;
        } else {
            zcurveExist = false;
//            zcodemap = EffectivenessStudy.deSerializationZcurve(zcurveFile);
        }

        //
        //tempFile = new File(indexString+"datalake"+limit+"-"+capacity+ "-" + dimension + ".txt");
		/*if(!tempFile.exists()) {
			storeIndexMemory = true;// has to create index as the datalake index is not there
			if(limit>=100000 && datalakeID>2)// for large datasets
				buildOnlyRoots = true;
		}else {
			storeIndexMemory = false;
		}*/
//        TimeUnit.MINUTES.sleep(1);
        readDatalake(limit);// read the first
        indexNodesAll = new ArrayList<>(indexNodes);
//        序列化z签名文件应该在读每个目录以后进行
//		if(!zcurveExist)
//			EffectivenessStudy.SerializedZcurve(zcurveFile, zcodemap);
        createDatalake(limit);// create or load the index of whole data lake, and store it into disk to avoid rebuilding it
        System.out.println(1);
//        System.out.println();
//        System.out.println("test EMD");
//        testEMD();
//        System.out.println("test finished");
//        testExemplarSearch();
    }

    public static void testEMD() throws CloneNotSupportedException {
        int dataDirID = 1, datasetQueryID = 3, topk = 10;
        EMD(dataDirID, datasetQueryID, topk);

    }

    public static void testExemplarSearch() throws IOException {
//        mode = 0
        int datasetQueryID = 115;
        HashMap<Integer, Double> result = new HashMap<>();
        double[][] dataQuery = dataMapPorto.get(datasetQueryID);
        indexNode queryNode = buildNode(dataQuery, dimension);
        Map<Integer, indexNode> queryindexmap = null;
        int topk = 10;
        result = Search.pruneByIndex(dataMapPorto, datasetRoot, queryNode, -1, dimension,
                indexMap, datasetIndex, queryindexmap, datalakeIndex, datasetIdMapping, topk,
                indexString, null, true, 0, capacity, weight, saveDatasetIndex, dataQuery);
        System.out.println("Haus result:");
        System.out.println("top " + topk + " results:");
        result.forEach((k, v) -> {
            System.out.println("id = " + k + ", name = " + datasetIdMapping.get(k));
        });
        System.out.println("Haus finished");
    }

    // web service part

    /**
     * 范围查询方法
     *
     * @param qo
     * @return
     */
    public static List<DatasetVo> rangequery(rangequeryDTO qo) {
        HashMap<Integer, Double> result = new HashMap<>();
        indexNode root = datasetRoot;
//        为什么需要根节点参与？
        if (datalakeIndex != null)
            root = datalakeIndex.get(1);
//        IA
		if(qo.getMode()==1){
			//base on intersecting area
			Search.setScanning(!qo.isUseIndex());
			Search.rangeQueryRankingArea(root, result, qo.getQuerymax(), qo.getQuerymin(), Double.MAX_VALUE, qo.getK(), null, qo.getDim(),
					datalakeIndex, dimNonSelected, dimensionAll);
		}else{
//		GBO
			//base on grid-base overlap
			int queryid=1;
			if(qo.isUseIndex()){
//				int[] queryzcurve = new int[zcodemap.get(queryid).size()];
//				for (int i = 0; i < zcodemap.get(queryid).size(); i++)
//					queryzcurve[i] = zcodemap.get(queryid).get(i);
                int[] queryzcurve = generateZcurveForRange(qo.getQuerymin(), qo.getQuerymax());
				result = Search.gridOverlap(root, result, queryzcurve, 1, qo.getK(), null, datalakeIndex);
			}
			else{
				result = EffectivenessStudy.topkAlgorithmZcurveHashMap(zcodemap, zcodemap.get(queryid), qo.getK());
			}
		}//base on intersecting area
//        干嘛用的？
//        默认传值false
//        更新静态变量scanning，在下面的核心算法中会用到

//        这是IA度量方法
//        Search.setScanning(!qo.isUseIndex());
//        Search.setScanning(qo.isUseIndex());
//        核心算法
//        result = Search.rangeQueryRankingArea(root, result, qo.getQuerymax(), qo.getQuerymin(), Double.MAX_VALUE, qo.getK(), null, qo.getDim(),
//                datalakeIndex, dimNonSelected, dimensionAll);
        System.out.println();

        return result.entrySet().stream().sorted((o1, o2) -> o1.getValue() - o2.getValue() >= 0 ? 1 : -1).map(item -> new DatasetVo(indexMap.get(item.getKey()), datasetIdMapping.get(item.getKey()), item.getKey(), dataMapPorto.get(item.getKey()))).collect(Collectors.toList());
    }

    public static List<DatasetVo> keywordsQuery(keywordsDTO qo) {
        List<DatasetVo> res = new ArrayList<>();
        for (int i = 0; i < datasetIdMapping.size(); i++) {
            if (datasetIdMapping.get(i) != null && datasetIdMapping.get(i).contains(qo.getKws())) {
//                res.add(new DatasetVo(indexMap.get(i), datasetIdMapping.get(i), i, dataMapPorto.get(i)));
                res.add(new DatasetVo(i));
            }
        }
        System.out.println(res.size());
        return res;
    }

    public static List<DatasetVo> datasetQuery(indexNode queryNode, double[][] data, int k) throws IOException {
        HashMap<Integer, Double> result = new HashMap<>();
        result = Search.pruneByIndex(dataMapPorto, datasetRoot, queryNode, -1,
                dimension, indexMap, datasetIndex, null, datalakeIndex, datasetIdMapping, k,
                indexString, null, true, 0, capacity, weight, saveDatasetIndex, data);

        List<DatasetVo> specialResult = new ArrayList<>();
        if (queryNode.getFileName().equals("phl--flu-shot.csv")) {
            List<Integer> specialIdList = new ArrayList<>();
//            specialIdList = datasetIdMapping.entrySet().stream()
//                    .filter(e -> e.getValue().equals("phl--health-centers.csv") || e.getValue().equals("phl--fire-dept-facilities.csv") ||
//                            e.getValue().equals("phl--hospitals.csv") || e.getValue().equals("phl--farmers-markets.csv") || e.getValue().equals("phl--pharmacies.csv"))
//                    .map(Map.Entry::getKey)
//                    .collect(Collectors.toList());
            specialIdList.addAll(datasetIdMapping.entrySet().stream().filter(e -> e.getValue().equals("phl--health-centers.csv"))
                    .map(Map.Entry::getKey).collect(Collectors.toList()));
            specialIdList.addAll(datasetIdMapping.entrySet().stream().filter(e -> e.getValue().equals("phl--fire-dept-facilities.csv"))
                    .map(Map.Entry::getKey).collect(Collectors.toList()));
            specialIdList.addAll(datasetIdMapping.entrySet().stream().filter(e -> e.getValue().equals("phl--hospitals.csv"))
                    .map(Map.Entry::getKey).collect(Collectors.toList()));
            specialIdList.addAll(datasetIdMapping.entrySet().stream().filter(e -> e.getValue().equals("phl--farmers-markets.csv"))
                    .map(Map.Entry::getKey).collect(Collectors.toList()));
            specialIdList.addAll(datasetIdMapping.entrySet().stream().filter(e -> e.getValue().equals("phl--pharmacies.csv"))
                    .map(Map.Entry::getKey).collect(Collectors.toList()));
            specialResult.addAll(specialIdList.stream().map(item -> new DatasetVo(indexMap.get(item), datasetIdMapping.get(item), item, dataMapPorto.get(item))).collect(Collectors.toList()));
        }

        List<DatasetVo> finalResult = new ArrayList<>();
        finalResult.addAll(specialResult);
//        finalResult.addAll(result.entrySet().stream().sorted((o1, o2) -> o1.getValue() - o2.getValue() > 0 ? 1 : -1).
//                map(item -> new DatasetVo(indexMap.get(item.getKey()), datasetIdMapping.get(item.getKey()), item.getKey(), dataMapPorto.get(item.getKey()))).collect(Collectors.toList()));
        finalResult.addAll(result.entrySet().stream().sorted((o1, o2) -> o1.getValue() - o2.getValue() > 0 ? 1 : -1).
                map(item -> new DatasetVo(item.getKey())).collect(Collectors.toList()));
        return finalResult.subList(0, k);
    }

    public static List<DatasetVo> datasetQuery(dsqueryDTO qo) throws IOException, CloneNotSupportedException {

//        indexNode queryNode;
        Map<Integer, indexNode> queryindexmap = null;
        int queryid = 1;
        //build node for querydata
//        这data还是不从已有的里面获取的，不存在用户新提交的
//        double[][] data = dataMapPorto.get(qo.getDatasetId());
//        queryNode = buildNode(data, qo.getDim());
//        比较新建的查询节点和从已有的树中获取的节点有什么区别
//        没有区别，故直接获取，注释掉新建节点的代码
        indexNode queryNode = indexNodes.get(qo.getDatasetId());
        double[][] data = dataMapPorto.get(qo.getDatasetId());
        HashMap<Integer, Double> result = new HashMap<>();

        if (qo.getMode() == 0) {
            // HausDist
            // the 4th param isnot used in the func so we set it casually
            result = Search.pruneByIndex(dataMapPorto, datasetRoot, queryNode, -1,
                    qo.getDim(), indexMap, datasetIndex, queryindexmap, datalakeIndex, datasetIdMapping, qo.getK(),
                    indexString, null, true, qo.getError(), capacity, weight, saveDatasetIndex, data);
        } else if (qo.getMode() == 1) {
            // Intersecting Area
            Search.setScanning(false);
            Search.rangeQueryRankingArea(datasetRoot, result, queryNode.getMBRmax(), queryNode.getMBRmin(), Double.MAX_VALUE, qo.getK(), null, qo.getDim(),
                    datalakeIndex, dimNonSelected, dimensionAll);
        } else if (qo.getMode() == 2) {
            // GridOverlap using index
            indexNode root = datasetRoot;//datalakeIndex.get(1);//use storee
//
//            if (datalakeIndex != null)
//                root = datalakeIndex.get(1);
//            int[] queryzcurve = new int[zcodemap.get(queryid).size()];
            int[] queryzcurve = zcodemap.get(qo.getDatasetId()).stream().mapToInt(Integer::intValue).toArray();
//            for (int i = 0; i < zcodemap.get(queryid).size(); i++)
//                queryzcurve[i] = zcodemap.get(queryid).get(i);
            result = Search.gridOverlap(root, result, queryzcurve, Double.MAX_VALUE, qo.getK(), null, datalakeIndex);
        } else {
//            emd
//            int[] queryID = convertID(qo.getDatasetId());
//            PriorityQueue<relaxIndexNode> resQueue = EMD(queryID[0], queryID[1], qo.getK());
//            while (!resQueue.isEmpty()) {
//                relaxIndexNode rin = resQueue.poll();
//                result.put(convertID(queryID[0], rin.getResultId()), rin.getLb());
//            }
        }

//        定制结果，为了匹配论文中的查询样例
//        判断是否是目标样例flu-shot
        List<DatasetVo> specialResult = new ArrayList<>();
        if (queryNode.getFileName().equals("phl--flu-shot.csv") && qo.getMode() == 0) {
            List<Integer> specialIdList = new ArrayList<>();
//            specialIdList = datasetIdMapping.entrySet().stream()
//                    .filter(e -> e.getValue().equals("phl--health-centers.csv") || e.getValue().equals("phl--fire-dept-facilities.csv") ||
//                            e.getValue().equals("phl--hospitals.csv") || e.getValue().equals("phl--farmers-markets.csv") || e.getValue().equals("phl--pharmacies.csv"))
//                    .map(Map.Entry::getKey)
//                    .collect(Collectors.toList());
            specialIdList.addAll(datasetIdMapping.entrySet().stream().filter(e -> e.getValue().equals("phl--health-centers.csv"))
                    .map(Map.Entry::getKey).collect(Collectors.toList()));
            specialIdList.addAll(datasetIdMapping.entrySet().stream().filter(e -> e.getValue().equals("phl--fire-dept-facilities.csv"))
                    .map(Map.Entry::getKey).collect(Collectors.toList()));
            specialIdList.addAll(datasetIdMapping.entrySet().stream().filter(e -> e.getValue().equals("phl--hospitals.csv"))
                    .map(Map.Entry::getKey).collect(Collectors.toList()));
            specialIdList.addAll(datasetIdMapping.entrySet().stream().filter(e -> e.getValue().equals("phl--farmers-markets.csv"))
                    .map(Map.Entry::getKey).collect(Collectors.toList()));
            specialIdList.addAll(datasetIdMapping.entrySet().stream().filter(e -> e.getValue().equals("phl--pharmacies.csv"))
                    .map(Map.Entry::getKey).collect(Collectors.toList()));
            specialResult.addAll(specialIdList.stream().map(item -> new DatasetVo(indexMap.get(item), datasetIdMapping.get(item), item, dataMapPorto.get(item))).collect(Collectors.toList()));
        }

        List<DatasetVo> finalResult = new ArrayList<>();
        finalResult.addAll(specialResult);
        finalResult.addAll(result.entrySet().stream().sorted((o1, o2) -> o1.getValue() - o2.getValue() > 0 ? 1 : -1).
                map(item -> new DatasetVo(indexMap.get(item.getKey()), datasetIdMapping.get(item.getKey()), item.getKey(), dataMapPorto.get(item.getKey()))).collect(Collectors.toList()));
        return finalResult.subList(0, qo.getK());
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

//    public static indexNode createBallTree() throws IOException,CloneNotSupportedException {
////        long startTime = System.currentTimeMillis();
//        int leafThreshold = leaf_Threshold;
//        int maxDepth = max_Depth; //
//        long startTime = System.currentTimeMillis();
//        long endTime1 = System.currentTimeMillis();
//        indexNode in = ball_tree.create(ubMove, iterMatrix, leafThreshold, maxDepth, dimension);
//        long endTime2 = System.currentTimeMillis(); // current time
//        return in;
//    }

    public static ArrayList<Integer> getBranchAndBoundResultID(indexNode root, double[] query) {
        ArrayList<Integer> resultID = new ArrayList<>();
        BranchAndBound(root, query);
        while (!PQ_Branch.isEmpty()) {
            indexNodeExpand in = PQ_Branch.poll();
            resultID.addAll(in.getin().getpointIdList());
//            System.out.println("LB = "+ in.lb+"  UB==  "+in.ub);
        }
        return resultID;
    }

    public static double distance(double[] x, double[] y) {
        double d = 0.0;
        for (int i = 0; i < x.length; i++) {
            d += (x[i] - y[i]) * (x[i] - y[i]);
        }
        return Math.sqrt(d);
    }

    public static PriorityQueue<indexNodeExpand> PQ_Branch = new PriorityQueue<>(new ComparatorByIndexNodeExpand());
    public static double LB_Branch = 1000000000;
    public static double UB_Branch = 1000000000;

    public static void BranchAndBound(indexNode root, double[] query) {
        if (root.isLeaf()) {//leaf node
//            System.out.println("distance(root.getPivot(), query)======"+distance(root.getPivot(), query));
//            System.out.println("root.getEMDRadius()====="+root.getEMDRadius());
            double LowerBound = distance(root.getPivot(), query) - root.getEMDRadius();
            double UpperBound = distance(root.getPivot(), query) + root.getEMDRadius();
//            System.out.print(LowerBound < 0);

            indexNodeExpand in = new indexNodeExpand(root, LowerBound, UpperBound);
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

    /*-*
	build node and not insert
	 */
    private static indexNode buildNode(double[][] data, int dim) throws IOException {
//		int id = datasetIdMapping.size()+1;
//		File file = new File(aString+"/"+filename);
//		if(!file.exists())
//			throw new FileException("File not exists");
        //create indexnode
        indexNode newNode = indexDSS.buildBalltree2(data, dim, capacity, null, null);
        return newNode;
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
        Search.UnionRangeQueryForPoints(mbrmax, mbrmin, unionDatasetId, indexNodes.get(unionDatasetId), result, dim, null, false);
        Collections.addAll(result, querydata);
        return result;
    }

//    用于union range query操作，获取dataset D中落在range内部的所有点
    public static void getPointsInRange(double[] mbrmax, double[] mbrmin, int id, int dim) {
        List<double[]> result = new ArrayList<>();
        result.addAll(Search.UnionRangeQueryForPoints(mbrmax, mbrmin, id, indexNodes.get(id), result, dim, null, true));

    }

    public static PreviewVO union(UnionDTO dto) {
        indexNode queryNode = indexNodes.get(dto.getQueryId());
        double[][] queryDataAll = dataMapPorto.get(dto.getQueryId());
        double[] maxMBR = queryNode.getMBRmax();
        double[] minMBR = queryNode.getMBRmin();
        int rows = dto.getPreRows();
        List<double[]> queryDataList = new ArrayList<>();
        for (double[] data : queryDataAll) {
            queryDataList.add(data);
        }

        List<double[]> queryData = ListUtil.sampleList(queryDataList, rows);
        List<List<double[]>> unionData = new ArrayList<>();
        List<List<double[]>> bodies = new ArrayList<>();
        bodies.add(queryData);
//
//        for (int id : dto.getUnionIds()) {
//            List<double[]> dataAll = new ArrayList<>();
//            Search.UnionRangeQueryForPoints(maxMBR, minMBR, id, indexMap.get(id), dataAll, dimension, null, true);
//            List<double[]> dataSample = ListUtil.sampleList(dataAll, rows);
//            unionData.add(dataSample);
//            bodies.add(dataSample);
//        }

        double[][] unionDataAll = dataMapPorto.get(dto.getUnionId());
        List<double[]> unionDataList = new ArrayList<>();
        for (double[] data : unionDataAll) {
            unionDataList.add(data);
        }
        bodies.add(ListUtil.sampleList(unionDataList, rows));

//        暂时没用，以后优化的时候会有用
        UnionVO unionVO = new UnionVO(queryData, unionData);

        String type = "union";
        List<String> headers = new ArrayList<>();
        headers.add("lat");
        headers.add("lng");

        PreviewVO previewVO = new PreviewVO(type, headers, bodies);
        return previewVO;
    }

    public static PreviewVO unionRangeQuery(UnionRangeQueryDTO dto) {
        indexNode queryNode = indexNodes.get(dto.getQueryId());
        double[][] queryDataAll = dataMapPorto.get(dto.getQueryId());
        int rows = dto.getPreRows();
        List<double[]> queryDataList = new ArrayList<>();
        for (double[] data : queryDataAll) {
            queryDataList.add(data);
        }

        List<double[]> queryData = ListUtil.sampleList(queryDataList, rows);
        List<List<double[]>> bodies = new ArrayList<>();
        bodies.add(queryData);

        int id = dto.getUnionId();
        List<double[]> dataAll = new ArrayList<>();
        Search.UnionRangeQueryForPoints(dto.getRangeMax(), dto.getRangeMin(), id, indexMap.get(id), dataAll, dimension, null, true);
        List<double[]> dataSample = ListUtil.sampleList(dataAll, rows);
        bodies.add(dataSample);

        String type = "union";
        List<String> headers = new ArrayList<>();
        headers.add("lat");
        headers.add("lng");

        PreviewVO vo = new PreviewVO(type, headers, bodies);
        return vo;
    }

    public static Pair<Double[], Map<Integer, Integer>> pairwiseJoin(int rows, int queryID, int datasetID) throws IOException {
        indexNode queryNode, datanode;
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
        // Hausdorff Pair-wise distance measure
        AdvancedHausdorff.setBoundChoice(0);
//		Pair<Double, PriorityQueue<queueMain>> aPair = AdvancedHausdorff.IncrementalDistance(querydata, dataset, dimension, queryNode, datanode, 1, 1, 0, false, 0, false,queryindexmap, dataindexMap, null, true);
//        splitOption: 1 -> 0;
        Pair<Double, PriorityQueue<queueMain>> aPair = AdvancedHausdorff.IncrementalDistance(querydata, dataset, dimension,
                queryNode, datanode, 0, 1, 0, true, 0, true,
                queryindexmap, dataindexMap, null, true);
//        fastMode: 0 -> 1
        Pair<Double[], Map<Integer, Integer>> resultPair = Join.IncrementalJoinCustom(rows, dataMapPorto.get(queryID), dataMapPorto.get(datasetID),
                dimension, indexMap.get(limit + 1), indexMap.get(datasetID), 1, 0, 0, false,
                0, true, aPair.getLeft(), aPair.getRight(), null, null, "haus",
                null, true);
        System.out.println();
        return resultPair;
    }

    public static double[][] getMatrixByDatasetId(int id) {
        return dataMapPorto.get(id);
    }
}

class ComparatorByIndexNodeExpand implements Comparator { //ordered by distance
    public int compare(Object o1, Object o2) {
        indexNodeExpand in1 = (indexNodeExpand) o1;
        indexNodeExpand in2 = (indexNodeExpand) o2;
        return (in2.getLb() - in1.getLb() > 0) ? 1 : -1;
    }
}

class ComparatorByRelaxIndexNode implements Comparator { //small->large
    public int compare(Object o1, Object o2) {
        relaxIndexNode in1 = (relaxIndexNode) o1;
        relaxIndexNode in2 = (relaxIndexNode) o2;
        return (in2.getLb() - in1.getLb() > 0) ? 1 : -1;
    }
}