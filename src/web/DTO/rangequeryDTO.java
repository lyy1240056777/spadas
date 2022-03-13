package web.DTO;

import lombok.Data;

/**
 * @author Tian Qi Qing
 * @version 1.0
 * @date 2022/03/03/12:06
 **/
@Data
public class rangequeryDTO {
    int k=10; //top-k
    int dim; //dimension
    double[] querymax;    //[lat,lng]
    double[] querymin;
    boolean useIndex=true;
    int mode=1;
}
