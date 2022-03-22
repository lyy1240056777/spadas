package web.Utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Tian Qi Qing
 * @version 1.0
 * @date 2022/03/04/15:15
 **/
@ConfigurationProperties(prefix = "spadas.file")
@Component
@Data
public class FileProperties {

    private String baseUri;

    private String staticUri;
}