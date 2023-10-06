package web.controller;

import edu.rmit.trajectory.clustering.kmeans.indexNode;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import web.param.*;
import web.Utils.FileU;
import web.Utils.FileUtil;
import web.VO.DatasetVo;
import web.VO.JoinVO;
import web.VO.PreviewVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import web.service.FrameworkService;

import java.io.IOException;
import java.util.*;
import java.util.List;

@RestController
@CrossOrigin
public class BaseController {
    @Autowired
    private FileUtil fileService;

    @Autowired
    private FrameworkService framework;

    //    本来是设计的只传cityNode，但对于不存在城市形式的数据集集来说，需要重新设计传参
//    直接传List<indexNode>，因为indexNode中有些属性会被忽略，所以对象大小不会太大
    @RequestMapping(value = "spadas/api/load", method = RequestMethod.GET)
    public List<indexNode> loadData() {
        return framework.indexNodes;
    }

    @RequestMapping(value = "/spadas/api/getds", method = RequestMethod.GET)
    public Map<String, Object> getDatasetById(@RequestParam int id) {
//        DatasetVo vo = framework.getDatasetVO(id);
        // set options
        DatasetQueryParams options = new DatasetQueryParams();
        options.setK(10);
        options.setDim(2);
        options.setQuerydata(new double[1][]);
        options.setDatasetId(-1);
        //    0: Haus, 1: IA, 2: GBO, 3: EMD
        options.setMode(0);
        options.setError(0.0);
        options.setApproxi(true);
        options.setUseIndex(true);
        DatasetVo vo = framework.datasetAugment(id, 2, 1, options);
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
    @RequestMapping(value = "spadas/api/rangequery", method = RequestMethod.POST)
    public Map<String, Object> rangequery(@RequestBody RangeQueryParams qo) {
        HashMap<String, Object> result = new HashMap();
        result.put("nodes", framework.rangequery(qo));
        return result;
    }

    @RequestMapping(value = "spadas/api/keywordsquery", method = RequestMethod.POST)
    public Map<String, Object> keywordsquery(@RequestBody KeywordsParams qo) {
        return new HashMap() {{
            put("nodes", framework.keywordsQuery(qo));
        }};
    }

    @RequestMapping(value = "spadas/api/uploaddataset", method = RequestMethod.POST)
    public Map<String, Object> uploadDataset(@RequestParam("file") MultipartFile file, @RequestParam("filename") String filename, @RequestParam("k") int k) throws IOException {
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
    }

    @RequestMapping(value = "spadas/api/dsquery", method = RequestMethod.POST)
    public Map<String, Object> datasetQuery(@RequestBody DatasetQueryParams qo) throws IOException {
        List<DatasetVo> result = framework.datasetQuery(qo);
        return new HashMap() {{
            put("nodes", result);
        }};
    }

    @GetMapping("spadas/api/file/{id}")
    public void downloadFile(@PathVariable int id, HttpServletRequest request, HttpServletResponse response) {
        fileService.downloadFile(framework.fileIDMap.get(id), request, response);
    }

    @PostMapping("spadas/api/preview")
    public Map<String, Object> datasetPreview(@RequestBody PreviewParams dto) {
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

    @RequestMapping(value = "spadas/api/join", method = RequestMethod.GET)
    public JoinVO datasetJoin(@RequestParam int queryId, @RequestParam int datasetId, @RequestParam int rows) throws IOException {
        return framework.join(queryId, datasetId, rows);
    }

    @PostMapping("spadas/api/union")
    public PreviewVO datasetUnion(@RequestBody UnionParams dto) {
        String type = "union";
        List<String> headers = Arrays.asList("lat", "lng");
        List<List<double[]>> body = framework.union(dto);
        return new PreviewVO(type, headers, body);
    }

    @PostMapping("spadas/api/unionRangeQuery")
    public PreviewVO datasetUnionRangeQuery(@RequestBody UnionRangeQueryParams dto) {
        String type = "union";
        List<String> headers = Arrays.asList("lat", "lng");
        List<List<double[]>> body = framework.unionRangeQuery(dto);
        return new PreviewVO(type, headers, body);
    }
}
