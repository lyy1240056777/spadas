package web.VO;

import lombok.Data;

import java.util.List;

@Data
public class UnionVO {
    List<double[]> queryData;
    List<List<double[]>> unionData;

    public UnionVO(List<double[]> queryData, List<List<double[]>> unionData) {
        this.queryData = queryData;
        this.unionData = unionData;
    }
}
