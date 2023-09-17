package web.controller;

import edu.rmit.trajectory.clustering.kmeans.indexNode;
import com.github.xiaoymin.knife4j.annotations.DynamicParameter;
import com.github.xiaoymin.knife4j.annotations.DynamicResponseParameters;
import edu.nyu.dss.similarity.Framework;
import edu.nyu.dss.similarity.SSSData;
import edu.nyu.dss.similarity.SSSOperate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import web.DTO.*;
import web.Utils.FileU;
import web.Utils.FileUtil;
import web.VO.DatasetVo;
import web.VO.PreviewVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;


/**
 * @author Tian Qi Qing
 * @version 1.0
 **/

@Api
@RestController
@CrossOrigin
public class BaseController {
    @Autowired
    private FileUtil fileService;

    @Autowired
    private Framework framework;

    //    本来是设计的只传cityNode，但对于不存在城市形式的数据集集来说，需要重新设计传参
//    直接传List<indexNode>，因为indexNode中有些属性会被忽略，所以对象大小不会太大
    @RequestMapping(value = "spadas/api/load", method = RequestMethod.GET)
    public List<indexNode> loadData(@RequestParam int id) {
        return framework.indexNodes;
    }

    @ApiOperation("get dataset by id")
    @RequestMapping(value = "/spadas/api/getds", method = RequestMethod.GET)
    @ApiImplicitParam(name = "id", value = "dataset id")
    @DynamicResponseParameters(name = "SingleDatasetDataVO", properties = {
            @DynamicParameter(name = "node", value = "node summary", dataTypeClass = DatasetVo.class),
    })

    public Map<String, Object> getDatasetById(@RequestParam int id) {
        DatasetVo vo = framework.getDataset(id);
        HashMap<String, Object> res = new HashMap<>();
        res.put("node", vo);
        return res;
    }

    /**
     * range query 范围查询
     * 之前把城市查询和范围查询集成在了一起，现在不需要城市查询了，所以需要修改代码逻辑
     *
     * @param qo
     * @return
     */
    @ApiOperation("range query")
    @DynamicResponseParameters(name = "RangeQueryModel", properties = {
            @DynamicParameter(name = "nodes", value = "list of indexNode", dataTypeClass = indexNode.class),
    })
    @RequestMapping(value = "spadas/api/rangequery", method = RequestMethod.POST)
    public Map<String, Object> rangequery(@RequestBody rangequeryDTO qo) {
        HashMap<String, Object> result = new HashMap();
//        范围查询承担了查找对应城市的数据集的工作，所以当qo的city name为空时才是范围查询，否则直接返回对应city name下的节点
//        需要更改对应的前端state属性
//        专家建议：最好不要传索引结构
//        不需要城市查询了
        /*if (qo.getCityName() != "") {
//            result.put("nodes", Framework.cityIndexNodeMap.get(qo.getCityName()));
            List<indexNode> nodes = Framework.cityIndexNodeMap.get(qo.getCityName());
            List<DatasetVo> vos = new ArrayList<>();
            for (indexNode node : nodes) {
                vos.add(new DatasetVo(node));
            }
            result.put("nodes", vos);
        } else {
//            范围查询的算法函数
            result.put("nodes", Framework.rangequery(qo));
        }*/
//        直接调用范围查询方法
        result.put("nodes", framework.rangequery(qo));
        return result;
    }

    @ApiOperation("keywords query")
    @DynamicResponseParameters(name = "KeywordsQueryModel", properties = {
            @DynamicParameter(name = "nodes", value = "list of indexNode", dataTypeClass = indexNode.class),
    })
    @RequestMapping(value = "spadas/api/keywordsquery", method = RequestMethod.POST)
    public Map<String, Object> keywordsquery(@RequestBody keywordsDTO qo) {
        return new HashMap() {{
            put("nodes", framework.keywordsQuery(qo));
        }};
    }


    @ApiOperation("upload dataset")
    @DynamicResponseParameters(name = "UploadModel", properties = {
            @DynamicParameter(name = "matrix", value = "2d array of uploaded dataset matrix", dataTypeClass = indexNode.class),
    })
    @RequestMapping(value = "spadas/api/uploaddataset", method = RequestMethod.POST)
//    public Map<String,Object> uploadDataset(@RequestParam("file") MultipartFile file) throws IOException {
//        String filename = fileService.uploadFile(file);
////        argo: 4
////        poi: 5
//        Framework.datalakeID=4;
////        call readSingleFile method
//        double [][] matrix = Framework.readSingleFile("/argoverse/"+filename);
//        return new HashMap(){{put("matrix",matrix);}};
//    }
    public Map<String, Object> uploadDataset(@RequestParam("file") MultipartFile file, @RequestParam("filename") String filename, @RequestParam("k") int k) throws IOException {
        System.out.println(file.getName());
        Pair<indexNode, double[][]> pair = framework.readNewFile(file, filename);
        List<DatasetVo> result = framework.datasetQuery(pair.getLeft(), pair.getRight(), k);
        if (pair == null) {
            return new HashMap() {{
                put("nodes", null);
            }};
        }
        return new HashMap() {{
            put("nodes", result);
            put("querynode", pair.getLeft());
            put("queryData", pair.getRight());
        }};
//        comment for safety
//        for (MultipartFile file : files) {
//            String fileName = fileService.uploadFile(file);
//        }


//        String fileName = fileService.uploadFile(file);
//        File[] files = fileFolder.listFiles();
//        Framework.readFolder(fileFolder, 270000);
//        String filename = fileService.uploadFile(file);
//        argo: 4
//        poi: 5
//        Framework.datalakeID=4;
//        call readSingleFile method
//        double [][] matrix = Framework.readSingleFile("/argoverse/"+filename);
//        return new HashMap(){{put("matrix",matrix);}};
    }

    @ApiOperation("query by dataset")
    @DynamicResponseParameters(name = "DatasetSearchModel", properties = {
            @DynamicParameter(name = "nodes", value = "list of DatasetVO", dataTypeClass = DatasetVo.class),
    })
    @RequestMapping(value = "spadas/api/dsquery", method = RequestMethod.POST)
//    样例查询
    public Map<String, Object> datasetQuery(@RequestBody dsqueryDTO qo) throws IOException, CloneNotSupportedException {
        List<DatasetVo> result = framework.datasetQuery(qo);
        return new HashMap() {{
            put("nodes", result);
        }};
    }

    @ApiOperation("download dataset")
    @GetMapping("spadas/api/file/{id}")
    public void downloadFile(@PathVariable int id, HttpServletRequest request, HttpServletResponse response) {
        fileService.downloadFile(framework.fileIDMap.get(id), request, response);
    }

    @ApiOperation("data preview after join or union")
    @DynamicResponseParameters(name = "PreviewModel", properties = {
            @DynamicParameter(name = "headers", value = "headers of csv, Type: List<String[]>"),
            @DynamicParameter(name = "bodies", value = "bodies of csv, Type: List<String[][]>"),
    })
    @PostMapping("spadas/api/preview")
    public Map<String, Object> datasetPreview(@RequestBody PreviewDTO dto) {
        List<String[]> headers = new ArrayList<>();
        List<String[][]> bodies = new ArrayList<>();
        dto.getIds().forEach(id -> {
            try {
                Pair<String[], String[][]> pair = FileU.readPreviewDataset(framework.fileIDMap.get(id), dto.getRows(), framework.dataMapPorto.get(id));
                headers.add(pair.getLeft());
                bodies.add(pair.getRight());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return new HashMap() {{
            put("headers", headers);
            put("bodies", bodies);
        }};
    }


    @ApiOperation("dataset join")
    @RequestMapping(value = "spadas/api/join", method = RequestMethod.GET)
//    @GetMapping("/join")
    public Map<String, Object> datasetJoin(@RequestParam int queryId, @RequestParam int datasetId, @RequestParam int rows) throws IOException {
        Pair<Double[], Map<Integer, Integer>> pair = framework.pairwiseJoin(rows, queryId, datasetId);
        Map<Integer, Integer> map = pair.getRight();
        double[][] queryData = framework.dataMapPorto.get(queryId);
        double[][] datasetData = framework.dataMapPorto.get(datasetId);
        Pair<String[], String[][]> querydata = FileU.readPreviewDataset(framework.fileIDMap.get(queryId), rows, queryData);
        Pair<String[], String[][]> basedata = FileU.readPreviewDataset(framework.fileIDMap.get(datasetId), Integer.MAX_VALUE, datasetData);

        //int len = querydata.getRight()[0].length+basedata.getRight()[0].length;
        String[] distHeader = {"distance(km)"};
        String[] joinHeaderTemp = ArrayUtils.addAll(querydata.getLeft(), distHeader);
        String[] joinHeader = ArrayUtils.addAll(joinHeaderTemp, basedata.getLeft());
//        List<String[]> joindata = pair.getRight().entrySet().stream()
//                .map(idPair -> ArrayUtils.addAll(querydata.getRight()[idPair.getValue()], basedata.getRight()[idPair.getValue()])).collect(Collectors.toList());
        List<String[]> joinData = new ArrayList<>();
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
            joinData.add(ArrayUtils.addAll(ArrayUtils.addAll(queryEntry, distEntryTemp), baseEntry));
        }
        return new HashMap() {{
            put("header", joinHeader);
            put("joinData", joinData);
        }};
    }

//    Union操作
//    @RequestMapping(value = "spadas/api/union", method = RequestMethod.POST)
//    public Map<String, indexNode> datasetUnion(@RequestBody dsqueryDTO qo) throws IOException {
//        Framework.UnionRangeQuery(qo.getQuerydata(), qo.getDatasetId(), 2);
////        为啥返回这种东西？？？
//        Map<String, indexNode> res = new HashMap<>();
//        res.put("nodes", Framework.indexMap.get(1));
//        return res;
//    }

    @PostMapping("spadas/api/union")
    public PreviewVO datasetUnion(@RequestBody UnionDTO dto) {
        String type = "union";
        List<String> headers = Arrays.asList("lat", "lng");
        List<List<double[]>> body = framework.union(dto);
        return new PreviewVO(type, headers, body);
    }

    @PostMapping("spadas/api/unionRangeQuery")
    public PreviewVO datasetUnionRangeQuery(@RequestBody UnionRangeQueryDTO dto) {
        String type = "union";
        List<String> headers = Arrays.asList("lat", "lng");
        List<List<double[]>> body = framework.unionRangeQuery(dto);
        return new PreviewVO(type, headers, body);
    }

    @GetMapping("spadas/api/sss/load")
    public Map<Integer, Double[]> loadSSS() throws IOException {
        if (SSSOperate.placeIDMap.isEmpty()) {
            SSSOperate.initSSS();
        }
        return SSSOperate.placeIDMap;
    }

    @GetMapping("spadas/api/sss/get/{param}")
    public List<SSSData> getSSSData(@PathVariable int param) throws IOException {
        return SSSOperate.selectSSSData(param);
    }

    @GetMapping("spadas/api/sss/file/{id}")
    public void downloadSSSFile(@PathVariable int id, HttpServletRequest request, HttpServletResponse response) {
        System.out.println("call downloadFile method, id = " + id);
        SSSData sss = SSSOperate.sssDataList.get(id);
        String fileName = sss.getName();
        File downloadFile = new File(SSSOperate.rootDir, fileName);
        System.out.println(downloadFile.isFile());
        fileService.downloadFile(downloadFile, request, response);
    }
}
