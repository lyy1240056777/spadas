package web.controller;


import au.edu.rmit.trajectory.clustering.kmeans.indexNode;
import edu.nyu.dss.similarity.Framework;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import web.DTO.dsqueryDTO;
import web.DTO.rangequeryDTO;
import web.Service.FileUtil;

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
        return new HashMap(){{put("nodes", Framework.indexNodes);}};
    }

    @RequestMapping(value = "/getds",method = RequestMethod.GET)
    public Map<String,Object> getDatasetById(@RequestParam int id) {
        return new HashMap(){{put("node", Framework.indexNodes.get(id-1));put("nodedata",Framework.dataMapPorto.get(id));}};
    }

    @RequestMapping(value = "/rangequery",method = RequestMethod.POST)
    public Map<String,Object> rangequery(@RequestBody rangequeryDTO qo) {
        return new HashMap(){{put("nodes",Framework.rangequery(qo));}};
    }

    @RequestMapping(value = "/uploaddataset",method = RequestMethod.POST)
    public Map<String,Object> uploadDataset(@RequestParam("file") MultipartFile file) {
        String filename = fileService.uploadFile(file);
        return new HashMap(){{put("filename",filename);}};
    }

    @RequestMapping(value = "/dsquery",method = RequestMethod.POST)
    public Map<String,Object> datasetQuery(@RequestBody dsqueryDTO qo) throws IOException {
        Pair<double[][],List<indexNode>> result =  Framework.datasetQuery(qo);
        List<double[][]> list = result.getRight().stream().map(node-> Framework.getMatrixByDatasetId(node.rootToDataset)).collect(Collectors.toList());

        return new HashMap(){{put("nodes",result.getRight());put("queryNode",result.getLeft());put("matrix",list);}};
    }


    @RequestMapping(value = "/test",method = RequestMethod.GET)
    public Map<String,Object> test1() throws IOException {
        return new HashMap(){{put("nodes",Framework.indexNodes.get(1));}};

    }




}
