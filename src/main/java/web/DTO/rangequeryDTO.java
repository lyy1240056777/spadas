package main.java.web.DTO;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author Tian Qi Qing
 * @version 1.0
 * @date 2022/03/03/12:06
 **/
@Data
@ApiModel("range query DTO")
public class rangequeryDTO {
    int k=10; //top-k
    int dim; //dimension
    double[] querymax;    //[lat,lng]
    double[] querymin;
//    可能跟索引有关，一般情况下都是使用索引的，所以为真
    boolean useIndex=true;
    int mode=1;

    String cityName = "";
}
