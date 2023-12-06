package edu.nyu.dss.similarity;

import edu.nyu.dss.similarity.index.DatasetIDMapping;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import web.SpadasWebApplication;
import web.VO.DatasetVo;
import web.consts.QueryMode;
import web.param.DatasetQueryParams;
import web.service.FrameworkService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpadasWebApplication.class)
public class DatasetQueryTest {
    @Autowired
    private FrameworkService frameworkService;

    @Autowired
    private DatasetIDMapping datasetIdMapping;
    
    @Test
    public void generalDatasetQueryTest() throws IOException, CloneNotSupportedException {
        DatasetQueryParams params = new DatasetQueryParams();
        List<DatasetVo> vos = new ArrayList<>();
        int queryId = 300;
        params.setMode(QueryMode.Haus);
        params.setDatasetId(queryId);
        datasetIdMapping.get(queryId);
        vos = frameworkService.datasetQuery(params);
        for (DatasetVo vo : vos) {
            System.out.println(vo.getId() + " " + vo.getFilename());
        }
        System.out.println("*************");

        params.setMode(QueryMode.IA);
        vos = frameworkService.datasetQuery(params);
        for (DatasetVo vo : vos) {
            System.out.println(vo.getId() + " " + vo.getFilename());
        }
        System.out.println("*************");

//        params.setMode(QueryMode.GBO);
//        vos = frameworkService.datasetQuery(params);
//        for (DatasetVo vo : vos) {
//            System.out.println(vo.getId() + " " + vo.getFilename());
//        }
//        System.out.println("*************");

        params.setMode(QueryMode.EMD);
        vos = frameworkService.datasetQuery(params);
        for (DatasetVo vo : vos) {
            System.out.println(vo.getId() + " " + vo.getFilename());
        }
        System.out.println("*************");
    }
}
