package web.Utils;

import java.util.ArrayList;
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

    public static String listToString(List<Integer> list) {
        StringBuilder sb = new StringBuilder();
        for (int i : list) {
            sb.append(i);
            sb.append(" ");
        }
        return sb.substring(0, sb.length() - 1);
    }

    public static List<Integer> stringToList(String s) {
        String[] ss = s.split(" ");
        List<Integer> list = new ArrayList<>();
        for (String str : ss) {
            list.add(Integer.parseInt(str));
        }
        return list;
    }
}
