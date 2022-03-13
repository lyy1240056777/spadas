package web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;



/**
 * @author Tian Qi Qing
 * @version 1.0
 * @date 2022/03/01/12:41
 **/
@SpringBootApplication(scanBasePackages = {"web","edu"})
public class SpadasWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpadasWebApplication.class, args);
    }
}
