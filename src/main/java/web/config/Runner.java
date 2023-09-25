package web.config;

import edu.nyu.dss.similarity.Framework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Tian Qi Qing
 * @version 1.0
 * @date 2022/03/01/14:09
 **/
@Component
public class Runner implements ApplicationRunner {
    @Autowired
    Framework framework;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        framework.init();
    }
}
