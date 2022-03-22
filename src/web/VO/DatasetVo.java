package web.VO;

import au.edu.rmit.trajectory.clustering.kmeans.indexNode;
import lombok.Data;

/**
 * @author Tian Qi Qing
 * @version 1.0
 * @date 2022/03/13/22:12
 **/
@Data
public class DatasetVo {
    private indexNode node;
    private String filename;
    private int id;
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
