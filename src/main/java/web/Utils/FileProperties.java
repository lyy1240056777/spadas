package main.java.web.Utils;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Tian Qi Qing
 * @version 1.0
 * @date 2022/03/04/15:15
 **/

@Configuration
@Data
public class FileProperties {

    @Value("${spadas.file.baseUri}")
    private String baseUri = "./dataset";

    @Value("${spadas.file.baseUri}")
    private String staticUri = "./dataset";
}