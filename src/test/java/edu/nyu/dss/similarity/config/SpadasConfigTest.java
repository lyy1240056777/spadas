package edu.nyu.dss.similarity.config;

import edu.whu.config.SpadasConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import web.SpadasWebApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpadasWebApplication.class)
public class SpadasConfigTest {
    @Autowired
    private SpadasConfig config;

    @Test
    public void readTest() {
        Assert.assertNotNull(config.getFile());
    }
}
