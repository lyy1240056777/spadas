package edu.nyu.dss.similarity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spadas")
@Data
public class SpadasConfig {
    private int dimension;

    private int resolution;

    private int leafCapacity;

    private boolean cacheDataset;

    private boolean cacheIndex;

    private boolean saveIndex;

    private int frontendLimitation;

    private FileConfig file;

    @Data
    public static class FileConfig {
        private String baseUri;

        private String staticUri;
    }
}
