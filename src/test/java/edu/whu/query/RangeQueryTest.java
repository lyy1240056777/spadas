package edu.whu.query;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import web.SpadasWebApplication;
import web.VO.DatasetVo;
import web.param.RangeQueryParams;
import web.service.FrameworkService;

import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpadasWebApplication.class)
public class RangeQueryTest {
    @Autowired
    private FrameworkService frameworkService;

    @Test
    public void testRangeQuery() {
        RangeQueryParams rangeQueryParams = new RangeQueryParams();
        rangeQueryParams.setK(1000);
        rangeQueryParams.setDim(2);
        rangeQueryParams.setQuerymin(new double[]{21, 110});
        rangeQueryParams.setQuerymax(new double[]{24, 115});
        List<DatasetVo> vos = frameworkService.rangeQuery(rangeQueryParams);
        System.out.println();
    }
}
