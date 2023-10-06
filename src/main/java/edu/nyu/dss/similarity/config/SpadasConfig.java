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

    //    对于中国地图，resolution设置为7或8比较好
//    static int resolution = 3; // also a parameter to test the grid-based overlap, and the approximate hausdorff, and range query
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
