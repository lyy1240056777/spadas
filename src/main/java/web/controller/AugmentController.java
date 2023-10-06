package web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import web.VO.DatasetVo;
import web.param.AugmentParams;
import web.service.FrameworkService;

@Slf4j
@RestController
public class AugmentController {

    @Autowired
    private FrameworkService frameworkService;

    @GetMapping("/spadas/api/augment")
    public DatasetVo getDatasetAugment(@RequestBody AugmentParams params) {
        log.info("you are querying augment for dataset {}", params.getDatasetID());
        return frameworkService.datasetAugment(params.getDatasetID(), params.getDatasetCount(), params.getCandidateCount(), params.getOptions());
    }
}
