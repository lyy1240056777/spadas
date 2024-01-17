package edu.whu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
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

    /**
     * 指定数据文件夹创建对应的路网
     */
    private String defaultRoadmap;

    private int slidePerSide;

    private int distanceLimit;

    private double unitPrice;
    
    private RoadmapConfig[] roadMaps;

    private FileConfig file;

    @Data
    public static class FileConfig {
        private String baseUri;

        private String staticUri;
    }
}
