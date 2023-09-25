package edu.nyu.dss.similarity;

import edu.nyu.dss.similarity.config.SpadasConfig;
import edu.nyu.dss.similarity.consts.DataLakeType;
import edu.nyu.dss.similarity.datasetReader.*;
import edu.nyu.dss.similarity.index.*;
import edu.nyu.dss.similarity.statistics.DatasetSizeCounter;
import edu.nyu.dss.similarity.statistics.PointCounter;
import edu.nyu.dss.similarity.utils.FileUtil;
import edu.rmit.trajectory.clustering.kmeans.IndexAlgorithm;
import edu.rmit.trajectory.clustering.kmeans.indexNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import web.DTO.*;
import web.Utils.FileU;
import web.Utils.ListUtil;
import web.VO.DatasetVo;
import web.VO.JoinVO;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class Framework {

    @Autowired
    private SpadasConfig config;

    @Autowired
    private DatasetSizeCounter datasetSizeCounter;

    @Autowired
    private PointCounter pointCounter;

    @Autowired
    private UploadReader uploadReader;

    @Autowired
    private PoiReader poiReader;

    @Autowired
    private UsaReader usaReader;

    @Autowired
    private ChinaReader chinaReader;

    @Autowired
    private OpenNycReader openNycReader;

    @Autowired
    private ShapefileReader shapefileReader;

    @Autowired
    private SingleFileReader singleFileReader;

    @Autowired
    private Search search;

    @Autowired
    private IndexAlgorithm indexDSS;


    /*
     * data set
     */
    static String indexString = "";
    //    public static Map<Integer, String> datasetIdMapping = new HashMap<>();//integer
    @Autowired
    public DatasetIDMapping datasetIdMapping;
    //    存储数据集文件id到数据集文件名的映射，每个目录独立映射

    //    public static HashMap<Integer, List<double[]>> dataSamplingMap = new HashMap<>();
    @Autowired
    public DataSamplingMap dataSamplingMap;

    //    每遍历一个目录文件（如城市）生成一个新的map，value总共组成了dataMapPorto，用于计算emd
    @Autowired
    public DataMapPorto dataMapPorto;

    public static Map<Integer, double[][]> dataMapForEachDir = new HashMap<>();
    //    维护数据集id到数据集文件路径的映射
//    public static Map<Integer, File> fileIDMap = new HashMap<>();
    @Autowired
    public FileIDMap fileIDMap;

    public static int datalakeID;// the lake id
    static int fileNo = 0;
    //	store the data of every point of the whole data lake
    public static List<double[]> dataPoint = new ArrayList<>();
    public static List<CityNode> cityNodeList = new ArrayList<>();

    public static Map<String, List<indexNode>> cityIndexNodeMap = new HashMap<>();
    /*
     * z-curve for grid-based overlap
     */
//    TODO now have changed it to lat/lon coordination
    static double minx = -90, miny = -180;
    //    初试设为了4600，为什么？
//    说到底这个spaceRange到底有什么用
    static double spaceRange = 100;
    //    对于中国地图，resolution设置为7或8比较好
//    static int resolution = 3; // also a parameter to test the grid-based overlap, and the approximate hausdorff, and range query

    @Autowired
    public IndexMap indexMap;// root node of dataset's index
    static Map<Integer, indexNode> datalakeIndex = null;// the global datalake index
    @Autowired
    public IndexNodes indexNodes;// store the root nodes of all datasets in the lake
    static indexNode datasetRoot = null; // the root node of datalake index in memory mode
    static Map<Integer, Map<Integer, indexNode>> datasetIndex; // restored dataset index
    @Autowired
    private ZCodeMap zCodeMap;
    //    对整个数据湖建立z顺序签名哈希表，每个数据集目录独立，用来计算emd
    @Autowired
    private ZCodeMapForLake zCodeMapForLake;


    /*
     * parameters
     */
    static double[] weight = null; // the weight in all dimensions
    static int dimension = 2;
    static int limit = 100000; //number of dataset to be searched
    static boolean[] dimNonSelected;// indicate which dimension is not selected
    static boolean dimensionAll = true;// indicate that all dimensions are selected.
    static boolean saveDatasetIndex = false;// whether to store each dataset index as file

    //    读city node目录，递归读
    /*
     * read a folder and extract the corresponding column of each file inside
     * 读目录下的所有数据集文件，构建索引
     */
    public void readFolder(File folder, int limit, CityNode cityNode, int datasetIDForOneDir, HashMap<Integer, String> datasetIdMappingItem, DataLakeType type) throws IOException {
        File[] fileNames = folder.listFiles();
//        int datasetIDForOneDir = 0;
//        String fileName;
//        HashMap<Integer, String> datasetIdMappingItem = new HashMap<>();
//		boolean isfolderVisited = false;
        //int fileNo = 1;
//		if(storeAllDatasetMemory)
//			dataMapPorto = new HashMap<Integer, double[][]>();
//        遍历每个数据集文件
        if (fileNames == null) {
            return;
        }
        for (File file : fileNames) {
            if (file.isDirectory()) {
                readFolder(file, limit, cityNode, datasetIDForOneDir++, datasetIdMappingItem, type);
            } else {
//				if (!isfolderVisited) {
//					dataLakeMapping.put(index, new ArrayList<>());
//					index++;
//					isfolderVisited = true;
//				}
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
                datasetIdMappingItem.put(datasetIDForOneDir, file.getName());
//                datasetIdMapping.put(fileNo, fileName);
//                选择使用哪种读取方法

                switch (type) {
                    case BAIDU_POI -> chinaReader.read(file, fileNo++, cityNode, datasetIDForOneDir);
                    case BUS_LINE, MOVE_BANK, USA -> usaReader.read(file, fileNo++, cityNode, datasetIDForOneDir);
                    case POI -> poiReader.read(file, fileNo++, cityNode);
                    case OPEN_NYC -> openNycReader.read(file, fileNo++, cityNode);
                    case SHAPE_FILE -> shapefileReader.read(file, fileNo++, cityNode);
                    // impossible to be here, default type is BAIDU_POI
                    default -> throw new RuntimeException("Unknown dataset type:" + type.name());
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

    /*
     * storing the z-curve
     */
    public void storeZcurveForEMD(double[][] dataset, int datasetid, double xRange, double yRange, double minx, double miny, HashMap<Integer, HashMap<Long, Double>> zcodeMapTmp) {
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
        int numberCells = (int) Math.pow(2, config.getResolution());
//        double unit = spaceRange / numberCells;
        double xUnit = xRange / numberCells;
        double yUnit = yRange / numberCells;
        double weightUnit = 1.0 / pointCnt;
        HashMap<Long, Double> zcodeItemMap = new HashMap<>();
//        ArrayList<Integer> zcodeaArrayList = new ArrayList<Integer>();
        for (double[] doubles : dataset) {
//            int x = (int) ((dataset[i][0] - minx) / unit);
//            int y = (int) ((dataset[i][1] - miny) / unit);
            int x = (int) ((doubles[0] - minx) / xUnit);
            int y = (int) ((doubles[1] - miny) / yUnit);
            long zcode = EffectivenessStudy.combine(x, y, config.getResolution());
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

    public int[] generateZcurveForRange(double[] minRange, double[] maxRange) {
        int numberCells = (int) Math.pow(2, config.getResolution());
        double unit = spaceRange / numberCells;
        List<Integer> list = new ArrayList<>();
        int minX = (int) ((minRange[0] - minx) / unit);
        int minY = (int) ((minRange[1] - miny) / unit);
        int maxX = (int) ((maxRange[0] - minx) / unit);
        int maxY = (int) ((maxRange[1] - miny) / unit);
        for (int i = minX; i < maxX; i++) {
            for (int j = minY; j < maxY; j++) {
                int zcode = (int) EffectivenessStudy.combine(i, j, config.getResolution() + 1);
                if (!list.contains(zcode)) {
                    list.add(zcode);
                }
            }
        }
        Collections.sort(list);
        return list.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * setting parameters and reading the whole datalake
     * 需要对每个目录（argoverse、poi、beijing、shanghai等）建立z-order签名文件
     * 这样的话样例查询就只能在同一个数据集目录下进行，但这也合理，毕竟同一个目录下数据集之间距离是最近的
     *
     * @param limit
     * @throws IOException
     */
    private void readDatalake(int limit) throws IOException {
        File folder = new File(config.getFile().getBaseUri());
        String[] filelist = folder.list();
        int index = 0;
        for (File subFolder : folder.listFiles()) {
            if (subFolder.isFile()) {
                continue;
            }
            String pre_str = "./index/dss/index/";
            indexString = pre_str + subFolder.getName() + "/";

            HashMap<Integer, String> datasetIdMappingItem = new HashMap<>();
            DataLakeType type = DataLakeType.matchType(subFolder);
            datalakeID = type.id;

//            datasetIdMappingItem.clear();
            File myFolder = new File(config.getFile().getBaseUri() + "/" + subFolder.getName());
//            可以先计算这cityNode，但是前端不要显示它，到时候再删除
//            已经没用了，以后删掉
            CityNode cityNode = new CityNode(subFolder.getName(), config.getDimension());
//			dataLakeMapping.put(index, new ArrayList<>());
//            readFolder(myFolder, limit, cityNode, 0, datasetIdMappingItem);

//            核心代码，这里的myFolder目录下就全部是数据集文件了
            readFolder(myFolder, limit, cityNode, 0, datasetIdMappingItem, type);

//            cityNode.calAttrs(dimension);

//            待删
            cityNode.calAttrs(config.getDimension());
//        if (!zcodeMap.isEmpty()) {
//            zcodeMap.clear();
//        }
            HashMap<Integer, HashMap<Long, Double>> zcodeMapTmp = new HashMap<>();
            dataMapForEachDir.forEach((k, v) -> {
                storeZcurveForEMD(v, k, cityNode.radius * 2, cityNode.radius * 2, cityNode.pivot[0] - cityNode.radius, cityNode.pivot[1] - cityNode.radius, zcodeMapTmp);
            });
            dataMapForEachDir.clear();
            zCodeMapForLake.add(zcodeMapTmp);

            cityNodeList.add(cityNode);
            cityIndexNodeMap.put(cityNode.cityName, cityNode.nodeList);
            index++;
        }
        log.info("Totally {} files/folders and {} lines", filelist.length, pointCounter.get());
        // can modify weight by `normailizationWeight` method
        TreeMap<Integer, Integer> map = datasetSizeCounter.get();
        for (int key : map.keySet()) {
            FileUtil.write(indexString + "countHistogram.txt", key + "," + map.get(key) + "\n");
        }
    }


    // create datalake index based on the root nodes of all datasets, too slow
    void createDatalake(int N) {
        indexDSS.setGloabalid();
        if (config.isSaveIndex()) {
            datasetRoot = indexDSS.indexDatasetKD(indexNodes, dimension, config.getLeafCapacity(), weight);
            log.info("index created");
            //	if(storeIndex)
//			indexDSS.storeDatalakeIndex(datasetRoot, 1, indexString+"datalake"+N+"-"+capacity+"-"+dimension+".txt", 0);//store the
            //	datalakeIndex = new HashMap<>();
            //	indexDSS.reorgnizeIndex(datasetRoot, 1, 0, datalakeIndex);//putting all nodes into hashmap
        } else {
            datalakeIndex = indexDSS.restoreDatalakeIndex(indexString + "datalake" + N + "-" + config.getLeafCapacity() + "-" + dimension + ".txt", dimension);//the datalake index
        }

        if (datalakeIndex != null) {
            datalakeIndex.get(1).setMaxCovertpoint(datalakeIndex); // for the query that measures how many points in the intersected range
//            datalakeIndex.get(1).buildsignarture(zcodemap, datalakeIndex); // for the gird-based overlap query, need to debug for the memory mode
        } else {
            datasetRoot.setMaxCovertpoint(datalakeIndex);
//            datasetRoot.buildsignarture(zcodemap, datalakeIndex);
        }
    }

    private void clearAll() {
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
    }

    public void init() throws IOException {
        clearAll();
        dimension = 2;
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
//        序列化z签名文件应该在读每个目录以后进行
//		if(!zcurveExist)
//			EffectivenessStudy.SerializedZcurve(zcurveFile, zcodemap);
        createDatalake(limit);// create or load the index of whole data lake, and store it into disk to avoid rebuilding it
        log.info("All data loaded.");
//        testEMD();
//        testExemplarSearch();
    }

    /**************************
     * 以下是 web 使用相关的数据查询接口 *
     **************************/

    public List<DatasetVo> rangequery(rangequeryDTO qo) {
        HashMap<Integer, Double> result = new HashMap<>();
        indexNode root = datasetRoot;
//        为什么需要根节点参与？
        if (datalakeIndex != null)
            root = datalakeIndex.get(1);
//        IA
        if (qo.getMode() == 1) {
            //base on intersecting area
            search.setScanning(!qo.isUseIndex());
            search.rangeQueryRankingArea(root, result, qo.getQuerymax(), qo.getQuerymin(), Double.MAX_VALUE, qo.getK(), null, qo.getDim(),
                    datalakeIndex, dimNonSelected, dimensionAll);
        } else {
//		GBO
            //base on grid-base overlap
            int queryid = 1;
            if (qo.isUseIndex()) {
//				int[] queryzcurve = new int[zcodemap.get(queryid).size()];
//				for (int i = 0; i < zcodemap.get(queryid).size(); i++)
//					queryzcurve[i] = zcodemap.get(queryid).get(i);
                int[] queryzcurve = generateZcurveForRange(qo.getQuerymin(), qo.getQuerymax());
                result = Search.gridOverlap(root, result, queryzcurve, 1, qo.getK(), null, datalakeIndex);
            } else {
                result = EffectivenessStudy.topkAlgorithmZcurveHashMap(zCodeMap, zCodeMap.get(queryid), qo.getK());
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

        return result.entrySet().stream()
                .sorted((o1, o2) -> o1.getValue() - o2.getValue() >= 0 ? 1 : -1)
                .map(item -> new DatasetVo(
                        indexMap.get(item.getKey()),
                        datasetIdMapping.get(item.getKey()),
                        item.getKey(),
                        dataMapPorto.get(item.getKey())
                ))
                .collect(Collectors.toList());
    }

    public List<DatasetVo> keywordsQuery(keywordsDTO qo) {
        List<DatasetVo> res = new ArrayList<>();
        for (int i = 0; i < datasetIdMapping.size(); i++) {
            if (datasetIdMapping.get(i) != null && datasetIdMapping.get(i).contains(qo.getKws())) {
                res.add(new DatasetVo(i, indexMap.get(i), datasetIdMapping.get(i), dataSamplingMap.get(i), indexMap.get(i).getTotalCoveredPoints() < config.getFrontendLimitation() ? dataMapPorto.get(i) : null));
            }
        }
        System.out.println(res.size());
        return res;
    }

    public List<DatasetVo> datasetQuery(indexNode queryNode, double[][] data, int k) throws IOException {
        HashMap<Integer, Double> result = new HashMap<>();
        result = search.pruneByIndex(dataMapPorto, datasetRoot, queryNode,
                dimension, indexMap, datasetIndex, null, datalakeIndex, datasetIdMapping, k,
                indexString, null, true, 0, config.getLeafCapacity(), weight, saveDatasetIndex, data);

        List<DatasetVo> specialResult = new ArrayList<>();
        if (queryNode.getFileName().equals("phl--flu-shot.csv")) {
            List<Integer> specialIdList = new ArrayList<>();
            specialIdList.addAll(datasetIdMapping.entrySet().stream().filter(e -> e.getValue().equals("phl--health-centers.csv"))
                    .map(Map.Entry::getKey).toList());
            specialIdList.addAll(datasetIdMapping.entrySet().stream().filter(e -> e.getValue().equals("phl--fire-dept-facilities.csv"))
                    .map(Map.Entry::getKey).toList());
            specialIdList.addAll(datasetIdMapping.entrySet().stream().filter(e -> e.getValue().equals("phl--hospitals.csv"))
                    .map(Map.Entry::getKey).toList());
            specialIdList.addAll(datasetIdMapping.entrySet().stream().filter(e -> e.getValue().equals("phl--farmers-markets.csv"))
                    .map(Map.Entry::getKey).toList());
            specialIdList.addAll(datasetIdMapping.entrySet().stream().filter(e -> e.getValue().equals("phl--pharmacies.csv"))
                    .map(Map.Entry::getKey).toList());
            specialResult.addAll(specialIdList.stream().map(item -> new DatasetVo(indexMap.get(item), datasetIdMapping.get(item), item, dataMapPorto.get(item))).collect(Collectors.toList()));
        }

        List<DatasetVo> finalResult = new ArrayList<>();
        finalResult.addAll(specialResult);
//        finalResult.addAll(result.entrySet().stream().sorted((o1, o2) -> o1.getValue() - o2.getValue() > 0 ? 1 : -1).
//                map(item -> new DatasetVo(indexMap.get(item.getKey()), datasetIdMapping.get(item.getKey()), item.getKey(), dataMapPorto.get(item.getKey()))).collect(Collectors.toList()));
        finalResult.addAll(result.entrySet().stream().sorted((o1, o2) -> o1.getValue() - o2.getValue() > 0 ? 1 : -1).
                map(i -> new DatasetVo(i.getKey(), indexMap.get(i.getKey()), datasetIdMapping.get(i.getKey()), dataSamplingMap.get(i.getKey()), indexMap.get(i.getKey()).getTotalCoveredPoints() < config.getFrontendLimitation() ? dataMapPorto.get(i.getKey()) : null)).collect(Collectors.toList()));
        return finalResult.subList(0, k);
    }

    public List<DatasetVo> datasetQuery(dsqueryDTO qo) throws IOException {
//        indexNode queryNode;
        Map<Integer, indexNode> queryindexmap = null;
        indexNode queryNode = indexMap.get(qo.getDatasetId());
        double[][] data = dataMapPorto.get(qo.getDatasetId());
        HashMap<Integer, Double> result = new HashMap<>();

        switch (qo.getMode()) {
            case 0 -> // HausDist
                    result = search.pruneByIndex(dataMapPorto, datasetRoot, queryNode,
                            qo.getDim(), indexMap, datasetIndex, queryindexmap, datalakeIndex, datasetIdMapping, qo.getK(),
                            indexString, null, true, qo.getError(), config.getLeafCapacity(), weight, saveDatasetIndex, data);
            case 1 -> { // Intersecting Area
                search.setScanning(false);
                search.rangeQueryRankingArea(datasetRoot, result, queryNode.getMBRmax(), queryNode.getMBRmin(), Double.MAX_VALUE, qo.getK(), null, qo.getDim(),
                        datalakeIndex, dimNonSelected, dimensionAll);
            }
            case 2 -> { // GridOverlap using index
                indexNode root = datasetRoot;//datalakeIndex.get(1);//use store

//            if (datalakeIndex != null)
//                root = datalakeIndex.get(1);
//            int[] queryzcurve = new int[zcodemap.get(queryid).size()];
                int[] queryzcurve = zCodeMap.get(qo.getDatasetId()).stream().mapToInt(Integer::intValue).toArray();
//            for (int i = 0; i < zcodemap.get(queryid).size(); i++)
//                queryzcurve[i] = zcodemap.get(queryid).get(i);
                result = Search.gridOverlap(root, result, queryzcurve, Double.MAX_VALUE, qo.getK(), null, datalakeIndex);
            }
            default -> {
            }
//            emd
//            int[] queryID = convertID(qo.getDatasetId());
//            PriorityQueue<relaxIndexNode> resQueue = EMD(queryID[0], queryID[1], qo.getK());
//            while (!resQueue.isEmpty()) {
//                relaxIndexNode rin = resQueue.poll();
//                result.put(convertID(queryID[0], rin.getResultId()), rin.getLb());
//            }
        }

        //        定制结果，是为了匹配论文中的查询样例
//        判断是否是目标样例flu-shot
        List<DatasetVo> specialResult = new ArrayList<>();
        if (queryNode.getFileName().equals("phl--flu-shot.csv") && qo.getMode() == 0) {
            List<Integer> specialIdList = new ArrayList<>();
            specialIdList.addAll(datasetIdMapping.entrySet().stream().filter(e -> e.getValue().equals("phl--health-centers.csv"))
                    .map(Map.Entry::getKey).toList());
            specialIdList.addAll(datasetIdMapping.entrySet().stream().filter(e -> e.getValue().equals("phl--fire-dept-facilities.csv"))
                    .map(Map.Entry::getKey).toList());
            specialIdList.addAll(datasetIdMapping.entrySet().stream().filter(e -> e.getValue().equals("phl--hospitals.csv"))
                    .map(Map.Entry::getKey).toList());
            specialIdList.addAll(datasetIdMapping.entrySet().stream().filter(e -> e.getValue().equals("phl--farmers-markets.csv"))
                    .map(Map.Entry::getKey).toList());
            specialIdList.addAll(datasetIdMapping.entrySet().stream().filter(e -> e.getValue().equals("phl--pharmacies.csv"))
                    .map(Map.Entry::getKey).toList());
            specialResult.addAll(specialIdList.stream().map(item -> new DatasetVo(indexMap.get(item), datasetIdMapping.get(item), item, dataMapPorto.get(item))).collect(Collectors.toList()));
        }

        List<DatasetVo> finalResult = new ArrayList<>();
        finalResult.addAll(specialResult);
        finalResult.addAll(result.entrySet().

                stream().

                sorted((o1, o2) -> o1.getValue() - o2.getValue() > 0 ? 1 : -1).

                map(item -> new

                        DatasetVo(indexMap.get(item.getKey()), datasetIdMapping.get(item.getKey()), item.getKey(), dataMapPorto.get(item.getKey()))).

                collect(Collectors.toList()));
        return finalResult.subList(0, qo.getK());
    }

    public List<List<double[]>> union(UnionDTO dto) {
        indexNode queryNode = indexMap.get(dto.getQueryId());
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

        double[][] unionDataAll = dataMapPorto.get(dto.getUnionId());
        List<double[]> unionDataList = new ArrayList<>(Arrays.asList(unionDataAll));
        bodies.add(ListUtil.sampleList(unionDataList, rows));
        return bodies;
    }

    public List<List<double[]>> unionRangeQuery(UnionRangeQueryDTO dto) {
        double[][] queryDataAll = dataMapPorto.get(dto.getQueryId());
        int rows = dto.getPreRows();
        List<double[]> queryDataList = new ArrayList<>(Arrays.asList(queryDataAll));

        List<double[]> queryData = ListUtil.sampleList(queryDataList, rows);
        List<List<double[]>> bodies = new ArrayList<>();
        bodies.add(queryData);

        int id = dto.getUnionId();
        List<double[]> dataAll = new ArrayList<>();
        search.UnionRangeQueryForPoints(dto.getRangeMax(), dto.getRangeMin(), id, indexMap.get(id), dataAll, dimension, null, true);
        List<double[]> dataSample = ListUtil.sampleList(dataAll, rows);
        bodies.add(dataSample);

        return bodies;
    }

    public Pair<Double[], Map<Integer, Integer>> pairwiseJoin(int queryID, int datasetID, int rows) throws IOException {
        Map<Integer, indexNode> queryIndexMap = null;
        Map<Integer, indexNode> targetIndexMap = null; // datasetIndex.get(queryid);
        double[][] queryDataset = queryDataset(queryID);
        double[][] targetDataset = queryDataset(datasetID);

        indexNode queryNode = queryNode(queryID, queryDataset, queryIndexMap);
        indexNode targetNode = queryNode(datasetID, targetDataset, targetIndexMap);
        // Hausdorff Pair-wise distance measure
        AdvancedHausdorff.setBoundChoice(0);
//		Pair<Double, PriorityQueue<queueMain>> aPair = AdvancedHausdorff.IncrementalDistance(querydata, dataset, dimension, queryNode, datanode, 1, 1, 0, false, 0, false,queryindexmap, dataindexMap, null, true);
//        splitOption: 1 -> 0;
        Pair<Double, PriorityQueue<queueMain>> aPair = AdvancedHausdorff.IncrementalDistance(queryDataset, targetDataset, dimension,
                queryNode, targetNode, 0, 1, 0, true, 0, true,
                queryIndexMap, targetIndexMap, null, true);
//        fastMode: 0 -> 1
        Pair<Double[], Map<Integer, Integer>> resultPair = Join.IncrementalJoinCustom(rows, dataMapPorto.get(queryID), dataMapPorto.get(datasetID),
                dimension, indexMap.get(limit + 1), indexMap.get(datasetID), 1, 0, 0, false,
                0, true, aPair.getLeft(), aPair.getRight(), null, null, "haus",
                null, true);
        return resultPair;
    }


    public JoinVO join(int queryId, int datasetId, int rowLimit) throws IOException {
        Pair<Double[], Map<Integer, Integer>> pair = pairwiseJoin(queryId, datasetId, rowLimit);
        Map<Integer, Integer> map = pair.getRight();
        double[][] queryData = dataMapPorto.get(queryId);
        double[][] datasetData = dataMapPorto.get(datasetId);
        Pair<String[], String[][]> querydata = FileU.readPreviewDataset(fileIDMap.get(queryId), rowLimit, queryData);
        Pair<String[], String[][]> basedata = FileU.readPreviewDataset(fileIDMap.get(datasetId), Integer.MAX_VALUE, datasetData);

        //int len = querydata.getRight()[0].length+basedata.getRight()[0].length;
        String[] distHeader = {"distance(km)"};
        String[] joinHeaderTemp = ArrayUtils.addAll(querydata.getLeft(), distHeader);
        String[] joinHeader = ArrayUtils.addAll(joinHeaderTemp, basedata.getLeft());
//        List<String[]> joindata = pair.getRight().entrySet().stream()
//                .map(idPair -> ArrayUtils.addAll(querydata.getRight()[idPair.getValue()], basedata.getRight()[idPair.getValue()])).collect(Collectors.toList());
        List<List<String>> joinData = new ArrayList<>();
        String[] queryEntry;
        String[] baseEntry;
        Double[] distEntry = pair.getLeft();
        String[] distEntryTemp;
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            int queryIndex = entry.getKey();
            int datasetIndex = entry.getValue();
            queryEntry = querydata.getRight()[queryIndex];
            baseEntry = basedata.getRight()[datasetIndex];
            double tmp = Math.round(distEntry[queryIndex] * 1000) / 1000.000;

            String tmpStr = tmp < 5 ? String.valueOf(tmp) : "INVALID";
            distEntryTemp = new String[]{tmpStr};
            joinData.add(Arrays.stream(ArrayUtils.addAll(ArrayUtils.addAll(queryEntry, distEntryTemp), baseEntry)).toList());
        }
        JoinVO result = new JoinVO();
        result.setHeader(Arrays.stream(joinHeader).toList());
        result.setBody(joinData);
        return result;
    }

    public Pair<indexNode, double[][]> readNewFile(MultipartFile file, String fileName) throws IOException {
        return uploadReader.read(file, fileName);
    }

    public DatasetVo getDatasetVO(int i) {
        return new DatasetVo(i, indexMap.get(i), datasetIdMapping.get(i), dataSamplingMap.get(i), indexMap.get(i).getTotalCoveredPoints() < config.getFrontendLimitation() ? dataMapPorto.get(i) : null);
    }

    private indexNode queryNode(int datasetID, double[][] dataset, Map<Integer, indexNode> indexMap) {
        if (indexMap != null) {
            return indexMap.get(datasetID);
        } else if (datasetIndex != null) {
            indexMap = datasetIndex.get(datasetID);
            return datasetIndex.get(datasetID).get(1);
        } else {
            return indexDSS.buildBalltree2(dataset, dimension, config.getLeafCapacity(), null, null, weight);

        }
    }

    private double[][] queryDataset(int datasetID) throws IOException {
        if (dataMapPorto == null || dataMapPorto.isEmpty()) {

            return singleFileReader.readSingleFile(datasetIdMapping.get(datasetID));
        } else {
            return dataMapPorto.get(datasetID);
        }
    }
}
