package main.java.web.DTO;

import lombok.Data;

@Data
public class UnionRangeQueryDTO {
    int queryId;
    double[] rangeMax;
    double[] rangeMin;
    int unionId;
    int preRows;
}
