package edu.whu.dataAcquisition;

import edu.nyu.dss.similarity.Framework;
import edu.nyu.dss.similarity.index.SubgraphMap;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import web.SpadasWebApplication;
import web.param.DataAcqParams;
import web.service.FrameworkService;

import java.util.Map;
import java.util.Set;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpadasWebApplication.class)
public class DataAcquisitionTest {
    @Autowired
    private Framework framework;
    @Autowired
    private FrameworkService frameworkService;
    @Autowired
    private SubgraphMap subgraphMap;
    @Test
    public void generalDataAcqTest() {
        framework.preprocessForDataAcq();
        framework.generateConnectedSubgraphMap();
        DataAcqParams qo = new DataAcqParams();
        qo.setDim(2);
        qo.setQueryMin(new double[]{21, 110});
        qo.setQueryMax(new double[]{24, 115});
        qo.setBudget(300);
        frameworkService.dataAcquisition(qo);
        System.out.println();
    }
}
