package web.VO;

import edu.rmit.trajectory.clustering.kmeans.IndexNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DatasetVo {
    private IndexNode node;

    private String filename;

    private int id;

    private double[][] matrix;

    private List<double[]> dataSamp;

    private HashMap<String, List<Object>> columns;

    private BigDecimal price;

    private int count;

    public DatasetVo(IndexNode node, String filename, int id, double[][] matrix) {
        this.node = node;
        this.filename = filename;
        this.id = id;
        this.matrix = matrix;
        this.dataSamp = new ArrayList<>();
        this.columns = new HashMap<>();
    }

    public DatasetVo(int id, IndexNode node, String fileName, List<double[]> dataSample, double[][] matrix, BigDecimal price) {
        this.id = id;
        this.node = node;
        this.filename = fileName;
        this.dataSamp = dataSample;
        this.matrix = matrix;
        this.columns = new HashMap<>();
        this.price = price;
    }
}
