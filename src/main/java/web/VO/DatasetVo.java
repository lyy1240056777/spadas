package web.VO;

import edu.rmit.trajectory.clustering.kmeans.indexNode;
import edu.nyu.dss.similarity.Framework;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

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
    private double[][] matrix;
    @ApiModelProperty("sampling data matrix")
    private List<double[]> dataSamp;

    public DatasetVo(indexNode node, String filename, int id, double[][] matrix) {
        this.node = node;
        this.filename = filename;
        this.id = id;
//        this.matrix = matrix;
    }

    public DatasetVo(int id, indexNode node, String fileName, List<double[]> dataSample, double[][] matrix) {
        this.id = id;
        this.node = node;
        this.filename = fileName;
        this.dataSamp = dataSample;
        this.matrix = matrix;
    }
}
