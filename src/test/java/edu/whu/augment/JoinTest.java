package edu.whu.augment;

import edu.nyu.dss.similarity.Framework;
import edu.nyu.dss.similarity.index.DatasetIDMapping;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import web.SpadasWebApplication;
import web.VO.JoinResultVO;
import web.service.FrameworkService;

import java.io.IOException;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpadasWebApplication.class)
public class JoinTest {
    @Autowired
    private FrameworkService frameworkService;

    @Autowired
    private DatasetIDMapping datasetIdMapping;

    @Test
    public void testJoin() throws IOException {
        int aId = 11;
        int bId = 30;

        long startTime = System.currentTimeMillis();
        JoinResultVO joinResultVO = frameworkService.join(aId, bId, 100000);
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        log.info("cost time : {}", elapsedTime);
        System.out.println();
    }
}
