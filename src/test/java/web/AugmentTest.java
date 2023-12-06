package web;

import edu.whu.tmeans.model.GeoLocation;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import web.VO.DatasetVo;
import web.consts.QueryMode;
import web.param.DatasetQueryParams;
import web.service.FrameworkService;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpadasWebApplication.class)
public class AugmentTest {
    @Autowired
    private FrameworkService service;

    private DatasetQueryParams options;

    @Before
    public void initOptions() {
        options = new DatasetQueryParams();
        options.setK(10);
        options.setDim(2);
        options.setQuerydata(new double[1][]);
        options.setDatasetId(-1);
        //    0: Haus, 1: IA, 2: GBO, 3: EMD
        options.setMode(QueryMode.Haus);
        options.setError(0.0);
        options.setApproxi(true);
        options.setUseIndex(true);
    }

    @Test
    public void RunAugmentTest() {
        int datasetID = 0;
        int k = 5;
        int num = 2;
        Long startTime = System.currentTimeMillis();
        DatasetVo result = service.datasetAugment(datasetID, k, num, options);
        Long endTime = System.currentTimeMillis();
        log.info("Brutal Force get Nearest {} Points from Top {} datasets costs {} ms", num, k, (endTime - startTime));
        Assert.assertEquals(result.getColumns().size(), k);
        result.getColumns().forEach((key, value) -> {
            System.out.println(key + ":");
            for (Object o : value) {
                if (o instanceof GeoLocation[]) {
                    GeoLocation[] tops = (GeoLocation[]) o;
                    StringBuilder sb = new StringBuilder("[");
                    for (GeoLocation loc : tops) {
                        sb.append("(");
                        sb.append(loc.getLat());
                        sb.append(",");
                        sb.append(loc.getLon());
                        sb.append("),");
                    }
                    sb.append("]");
                    System.out.println(sb);
                }
            }
        });

    }
}
