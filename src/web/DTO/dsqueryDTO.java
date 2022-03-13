package web.DTO;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tian Qi Qing
 * @version 1.0
 * @date 2022/03/04/14:33
 **/
@Data
public class dsqueryDTO {
    int k=10; //top-k
    int dim; //dimension
    double[][] querydata;
    int datasetId=-1;
    int mode = 0; //0 ApproHaus ,1 ExactHaus 2, Grid-based
    String dsFilename ;
    double error=0.0;
    boolean approxi = true;
    boolean useIndex = true;
}
