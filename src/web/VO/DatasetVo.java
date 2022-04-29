package web.VO;

import au.edu.rmit.trajectory.clustering.kmeans.indexNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Tian Qi Qing
 * @version 1.0
 * @date 2022/03/13/22:12
 **/
@Data
@ApiModel("Dataset ResponseVO")
public class DatasetVo {
    @ApiModelProperty("dataset root node")
    private indexNode node;
    @ApiModelProperty("dataset filename")
    private String filename;
    @ApiModelProperty("dataset id")
    private int id;
    @ApiModelProperty("2d data matrix")
    private double [][] matrix;

    public DatasetVo() {
    }

    public DatasetVo(indexNode node, String filename, int id, double[][] matrix) {
        this.node = node;
        this.filename = filename;
        this.id = id;
        this.matrix = matrix;
    }

    public DatasetVo(indexNode node, int id, double[][] matrix) {
        this.node = node;
        this.id = id;
        this.matrix = matrix;
    }
}
