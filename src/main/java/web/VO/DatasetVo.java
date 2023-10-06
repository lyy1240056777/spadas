package web.VO;

import edu.rmit.trajectory.clustering.kmeans.indexNode;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Data
public class DatasetVo {
    //    改为只传filename和id
//    已经是单一数据集级别了
    private indexNode node;

    private String filename;

    private int id;

    private double[][] matrix;

    private List<double[]> dataSamp;

    private HashMap<String, List<Object>> columns;

    public DatasetVo(indexNode node, String filename, int id, double[][] matrix) {
        this.node = node;
        this.filename = filename;
        this.id = id;
        this.matrix = matrix;
        this.dataSamp = new ArrayList<>();
        this.columns = new HashMap<>();
    }

    public DatasetVo(int id, indexNode node, String fileName, List<double[]> dataSample, double[][] matrix) {
        this.id = id;
        this.node = node;
        this.filename = fileName;
        this.dataSamp = dataSample;
        this.matrix = matrix;
        this.columns = new HashMap<>();
    }
}
