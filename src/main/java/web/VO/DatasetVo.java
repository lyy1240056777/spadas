package main.java.web.VO;

import au.edu.rmit.trajectory.clustering.kmeans.indexNode;
import edu.nyu.dss.similarity.Framework;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Tian Qi Qing
 * @version 1.0
 * @date 2022/03/13/22:12
 **/
@Data
@ApiModel("Dataset ResponseVO")
public class DatasetVo {
//    改为只传filename和id
//    已经是单一数据集级别了
    @ApiModelProperty("dataset root node")
    private indexNode node;
    @ApiModelProperty("dataset filename")
    private String filename;
    @ApiModelProperty("dataset id")
    private int id;
    @ApiModelProperty("2d data matrix")
    private double [][] matrix;
    @ApiModelProperty("sampling data matrix")
    private List<double[]> dataSamp;

    public DatasetVo() {
    }

    public DatasetVo(indexNode node, String filename, int id, double[][] matrix) {
        this.node = node;
        this.filename = filename;
        this.id = id;
//        this.matrix = matrix;
    }

    public DatasetVo(indexNode node, int id, double[][] matrix) {
        this.node = node;
        this.id = id;
//        this.matrix = matrix;
    }

    public DatasetVo(indexNode node) {
        this.node = node;
        this.id = node.getDatasetID();
        this.filename = Framework.datasetIdMapping.get(this.id);
        this.dataSamp = Framework.dataSamplingMap.get(this.id);
    }

    public DatasetVo(int id) {
        this.id = id;
        this.node = Framework.indexMap.get(id);
        this.filename = Framework.datasetIdMapping.get(id);
        this.dataSamp = Framework.dataSamplingMap.get(id);
//        数据量小就传，数据量大就不传
//        System.out.println(this.node.getTotalCoveredPoints());
        if (this.node.getTotalCoveredPoints() <= 10000) {
            this.matrix = Framework.dataMapPorto.get(id);
        } else {
            this.matrix = null;
        }
    }
}
