package main.java.web;

import main.java.web.Utils.FileProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.io.IOException;


/**
 * @author Tian Qi Qing
 * @version 1.0
 * @date 2022/03/01/12:41
 **/
@SpringBootApplication(scanBasePackages = {"main.java.web", "edu"})
public class SpadasWebApplication {
    public static void main(String[] args) throws IOException {
        SpringApplication.run(SpadasWebApplication.class, args);
    }
}
