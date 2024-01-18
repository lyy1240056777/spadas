package web.controller;

import edu.rmit.trajectory.clustering.kmeans.IndexNode;
import edu.whu.structure.Trajectory;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import web.VO.Result;
import web.consts.QueryMode;
import web.param.*;
import web.Utils.FileU;
import web.Utils.FileUtil;
import web.VO.DatasetVo;

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
    private FrameworkService frameworkService;

    @RequestMapping(value = "spadas/api/load", method = RequestMethod.GET)
    public List<IndexNode> loadData() {
        return frameworkService.indexNodes;
    }

    @RequestMapping(value = "/spadas/api/getds", method = RequestMethod.GET)
    public Map<String, Object> getDatasetById(@RequestParam int id) {
        DatasetQueryParams options = new DatasetQueryParams();
        options.setK(10);
        options.setDim(2);
        options.setQuerydata(new double[1][]);
        options.setDatasetId(-1);
        //    0: Haus, 1: IA, 2: GBO, 3: EMD
        options.setMode(QueryMode.Haus);
        options.setError(0.0);
        options.setApproxi(true);
        options.setUseIndex(true);
//        DatasetVo vo = framework.datasetAugment(id, 2, 1, options);
        DatasetVo vo = frameworkService.getDatasetVO(id);
        HashMap<String, Object> res = new HashMap<>();
        res.put("node", vo);
        return res;
    }

    /**
     * @param qo
     * @return
     */
    @RequestMapping(value = "spadas/api/rangequery", method = RequestMethod.POST)
    public Map<String, Object> rangeQuery(@RequestBody RangeQueryParams qo) {
        HashMap<String, Object> result = new HashMap();
        result.put("nodes", frameworkService.rangeQuery(qo));
        return result;
    }

    @RequestMapping(value = "spadas/api/keywordsquery", method = RequestMethod.POST)
    public HashMap<String, Object> keywordsQuery(@RequestBody KeywordsParams qo) {
        return new HashMap() {{
            put("nodes", frameworkService.keywordsQuery(qo));
        }};
    }

    @RequestMapping(value = "spadas/api/uploaddataset", method = RequestMethod.POST)
    public Map<String, Object> uploadDataset(@RequestParam("file") MultipartFile file, @RequestParam("filename") String filename, @RequestParam("k") int k) throws IOException {
        Pair<IndexNode, double[][]> pair = frameworkService.readNewFile(file, filename);
        List<DatasetVo> result = frameworkService.datasetQuery(pair.getLeft(), pair.getRight(), k);
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
    public Map<String, Object> datasetQuery(@RequestBody DatasetQueryParams qo) throws IOException, CloneNotSupportedException {
        List<DatasetVo> result = frameworkService.datasetQuery(qo);
        return new HashMap() {{
            put("nodes", result);
        }};
    }

    @GetMapping("spadas/api/file/{id}")
    public void downloadFile(@PathVariable int id, HttpServletRequest request, HttpServletResponse response) {
        fileService.downloadFile(frameworkService.fileIDMap.get(id), request, response);
    }

    @PostMapping("spadas/api/preview")
    public Map<String, Object> datasetPreview(@RequestBody PreviewParams dto) {
        List<String[]> headers = new ArrayList<>();
        List<String[][]> bodies = new ArrayList<>();
        dto.getIds().forEach(id -> {
            try {
                Pair<String[], String[][]> pair = FileU.readPreviewDataset(frameworkService.fileIDMap.get(id), dto.getRows(), frameworkService.dataMapPorto.get(id));
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


    @GetMapping("spadas/api/trajectory")
    public ArrayList<Trajectory> getTrajectory(@RequestParam(name = "id") int datasetID) {
        return frameworkService.getTrajectory(datasetID);
    }

    @PostMapping("spadas/api/data_acq")
    public Result dataAcquisition(@RequestBody DataAcqParams qo) {
        return Result.ok(frameworkService.dataAcquisition(qo));
    }
}
