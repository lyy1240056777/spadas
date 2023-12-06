package edu.whu.config;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class RoadmapConfig {
    private String name;

    private double[] latRange;

    private double[] lngRange;
}
