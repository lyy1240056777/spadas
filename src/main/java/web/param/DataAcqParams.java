package web.param;

import lombok.Data;

@Data
public class DataAcqParams {
    int dim;
    double[] queryMax;
    double[] queryMin;
    int budget;
}
