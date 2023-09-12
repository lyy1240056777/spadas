package web.config;

import edu.nyu.dss.similarity.Framework;
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
@Order(1)
public class Runner implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) throws Exception {
        Framework.init();
    }
}
