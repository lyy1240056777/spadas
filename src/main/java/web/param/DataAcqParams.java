package web.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class DataAcqParams {
    int dim;
    double[] queryMax;
    double[] queryMin;
    int budget;
}
