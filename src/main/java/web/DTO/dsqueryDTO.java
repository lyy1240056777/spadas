package web.DTO;

import io.swagger.annotations.ApiModel;
import lombok.Data;


/**
 * @author Tian Qi Qing
 * @version 1.0
 * @date 2022/03/04/14:33
 **/
@Data
@ApiModel("dataset query DTO")
public class dsqueryDTO {
    int k=10; //top-k
    int dim; //dimension
    double[][] querydata;
    int datasetId=-1;
//    0: Haus, 1: IA, 2: GBO, 3: EMD
    int mode = 0; //0 ApproHaus ,1 ExactHaus 2, Grid-based
    double error=0.0;
    boolean approxi = true;
    boolean useIndex = true;
}
