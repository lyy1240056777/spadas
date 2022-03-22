package web.controller;

import edu.nyu.dss.similarity.Framework;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import web.DTO.PreviewDTO;
import web.DTO.SingleArrayDTO;
import web.DTO.dsqueryDTO;
import web.DTO.rangequeryDTO;
import web.Utils.FileUtil;
import web.VO.DatasetVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author Tian Qi Qing
 * @version 1.0
 **/

@RestController
public class BaseController {
    @Autowired
    private FileUtil fileService;

    @RequestMapping(value = "/loaddata",method = RequestMethod.GET)
    public Map<String,Object> loadData() {
        return new HashMap(){{put("nodes", Framework.indexNodes); put("filenames",Framework.datasetIdMapping); }};
    }

    @RequestMapping(value = "/getds",method = RequestMethod.GET)
    public Map<String,Object> getDatasetById(@RequestParam int id) {
        return new HashMap(){{put("node", new DatasetVo(){{
            setNode(Framework.indexNodes.get(id));
            setId(id);
            setMatrix(Framework.dataMapPorto.get(id));
            setFilename(Framework.datasetIdMapping.get(id));
        }}
        );}};
    }

    @RequestMapping(value = "/rangequery",method = RequestMethod.POST)
    public Map<String,Object> rangequery(@RequestBody rangequeryDTO qo) {
        return new HashMap(){{put("nodes",Framework.rangequery(qo));}};
    }

    @RequestMapping(value = "/uploaddataset",method = RequestMethod.POST)
    public Map<String,Object> uploadDataset(@RequestParam("file") MultipartFile file) throws IOException {
        String filename = fileService.uploadFile(file);
        double [][] matrix = Framework.readSingleFile(filename);
        return new HashMap(){{put("matrix",matrix);}};
    }

    @RequestMapping(value = "/dsquery",method = RequestMethod.POST)
    public Map<String,Object> datasetQuery(@RequestBody dsqueryDTO qo) throws IOException {
        List<DatasetVo> result =  Framework.datasetQuery(qo);
        return new HashMap(){{put("nodes",result);}};
    }

    @GetMapping("/file/{filename:.+}")
    public void downloadFile(@PathVariable String filename, HttpServletRequest request, HttpServletResponse response) {
        fileService.downloadFile(filename, request, response);
    }

    @PostMapping("/preview")
    public Map<String,Object> datasetPreview(@RequestBody PreviewDTO dto) {
        List<String[]> headers = new ArrayList<>();
        List<String[][]> bodies = new ArrayList<>();
        dto.getIds().forEach(id->{
            try {
                Pair<String[],String[][]> pair = fileService.readPreviewDataset(Framework.datasetIdMapping.get(id),dto.getRows());
                headers.add(pair.getLeft());
                bodies.add(pair.getRight());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return new HashMap(){{put("headers",headers);put("bodies",bodies);}};
    }

//    @GetMapping("/preview")
//    public Map<String,Object> datasetPreview(@RequestParam int id) throws IOException {
//        Pair<String[],String[][]> pair = fileService.readPreviewDataset(Framework.datasetIdMapping.get(id));
//        return new HashMap(){{put("header", pair.getLeft());put("bodies", pair.getRight());}};
//    }

    @GetMapping("/join")
    public Map<String,Object> datasetJoin(@RequestParam int queryid,@RequestParam int datasetid) throws IOException {
        Pair<ArrayList<Double>,Map<Integer, Integer>> pair = Framework.pairwiseJoin(queryid,datasetid);
        Pair<String[],String[][]> querydata = fileService.readPreviewDataset(Framework.datasetIdMapping.get(queryid),Integer.MAX_VALUE);
        Pair<String[],String[][]> basedata = fileService.readPreviewDataset(Framework.datasetIdMapping.get(datasetid),Integer.MAX_VALUE);

        //int len = querydata.getRight()[0].length+basedata.getRight()[0].length;
        String[] joinHeader = ArrayUtils.addAll(querydata.getLeft(), basedata.getLeft());
        List<String[]> joindata = pair.getRight().entrySet().stream()
                .map(idPair-> ArrayUtils.addAll(querydata.getRight()[idPair.getKey()],basedata.getRight()[idPair.getKey()])).collect(Collectors.toList());
        return new HashMap(){{put("header",joinHeader);put("joinData",joindata);}};
    }



    @RequestMapping(value = "/test",method = RequestMethod.GET)
    public Map<String,Object> test1() throws IOException {
        return new HashMap(){{put("nodes",Framework.indexNodes.get(1));}};

    }




}
