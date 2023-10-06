package web.VO;

import edu.nyu.dss.similarity.CityNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CityVo {
    private List<CityNode> cityNodeList;
}
