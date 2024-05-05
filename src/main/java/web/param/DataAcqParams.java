package web.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataAcqParams {
    int dim;
    double[] queryMax;
    double[] queryMin;
    BigDecimal budget;
}
