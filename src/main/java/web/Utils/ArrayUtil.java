package web.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tian Qi Qing
 * @version 1.0
 * @date 2022/03/19/13:39
 **/
public class ArrayUtil {
    static double[][] concat(double[][] a1, double[][] a2){
        List<double[]> result = new ArrayList<double[]>();
        for(double[] entry: a1) {
            result.add(entry);
        }
        for(double[] entry: a2) {
            result.add(entry);
        }
        double[][] resultType = {};

        return result.toArray(resultType);
    }
}
