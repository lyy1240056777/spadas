package web.param;

import lombok.Data;

@Data
public class UnionRangeQueryParams {
    int queryId;
    double[] rangeMax;
    double[] rangeMin;
    int unionId;
    int preRows;
}
