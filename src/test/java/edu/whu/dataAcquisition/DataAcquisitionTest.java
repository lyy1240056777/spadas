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

import java.math.BigDecimal;
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
        DataAcqParams qo = new DataAcqParams();
        qo.setDim(2);
        BigDecimal[] arr = new BigDecimal[]{new BigDecimal("10"), new BigDecimal("100"), new BigDecimal("500"), new BigDecimal("1000")};
        qo.setQueryMin(new double[]{22, 112});
        qo.setQueryMax(new double[]{23, 113});
        for (BigDecimal b : arr) {
            qo.setBudget(b);
            dataAcq(qo);
        }
        qo.setBudget(BigDecimal.valueOf(300));
        double[] brr = new double[]{2.45, 2.4, 2, 0};
        for (double r : brr) {
            qo.setQueryMin(new double[]{28 + r, 118 + r});
            qo.setQueryMax(new double[]{33 - r, 123 - r});
            dataAcq(qo);
        }
//        dataAcq(qo);
        System.out.println();
    }

    private void dataAcq(DataAcqParams qo) {
        long startTime = System.currentTimeMillis();
        frameworkService.dataAcquisition(qo);
        long endTime = System.currentTimeMillis();
        long elapseTime = endTime - startTime;
        log.info("cost time : {}", elapseTime);
    }
}
