package web.param;

import lombok.Data;

@Data
public class RangeQueryParams {
    int k=10; //top-k
    int dim; //dimension
    double[] querymax;    //[lat,lng]
    double[] querymin;
//    可能跟索引有关，一般情况下都是使用索引的，所以为真
    boolean useIndex=true;
    int mode=1;

    String cityName = "";
}
