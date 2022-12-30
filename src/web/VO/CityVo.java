package web.VO;

import au.edu.rmit.trajectory.clustering.kmeans.indexNode;
import edu.nyu.dss.similarity.CityNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@ApiModel("City ResponseVO")
public class CityVo {
    @ApiModelProperty("city node list")
    private List<CityNode> cityNodeList;
//    @ApiModelProperty("index node list")
//    private ArrayList<indexNode> indexNodes;
//    @ApiModelProperty("dataset id mapping")
//    private Map<Integer, String> datasetIdMapping;

    public CityVo() {
    }

    //    public CityVo(List<CityNode> cityNodeList, ArrayList<indexNode> indexNodes, Map<Integer, String> datasetIdMapping) {
//        this.cityNodeList = cityNodeList;
//        this.indexNodes = indexNodes;
//        this.datasetIdMapping = datasetIdMapping;
//    }
    public CityVo(List<CityNode> cityNodeList) {
        this.cityNodeList = cityNodeList;
//        this.indexNodes = indexNodes;
//        this.datasetIdMapping = datasetIdMapping;
    }
}
