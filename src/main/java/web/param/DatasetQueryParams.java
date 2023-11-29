package web.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatasetQueryParams {
    int k = 10; //top-k
    int dim; //dimension
    double[][] querydata;
    int datasetId = -1;
    //    0: Haus, 1: IA, 2: GBO, 3: EMD
    int mode = 0; //0 ApproHaus ,1 ExactHaus 2, Grid-based
    double error = 0.0;
    boolean approxi = true;
    boolean useIndex = true;
}
