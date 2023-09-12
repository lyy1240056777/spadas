package main.java.web.Utils;

import java.util.Collections;
import java.util.List;

public class ListUtil {
    public static <T> List<T> sampleList(List<T> sourceList, int k) {
        if (sourceList.size() <= k) {
            return sourceList;
        }
        Collections.shuffle(sourceList);
        return sourceList.subList(0, k);
    }
}
