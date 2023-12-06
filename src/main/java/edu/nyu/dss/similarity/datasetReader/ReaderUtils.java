package edu.nyu.dss.similarity.datasetReader;

import java.util.Map;

public class ReaderUtils {

    /*
    Find the position columns from (table) headers.
     */
    public static String findLatKeyByHeader(Map<String, Integer> headers) {
        if (headers.containsKey("lat")) {
            return "lat";
        }
        if (headers.containsKey("latitude")) {
            return "latitude";
        }
        return "";
    }

    public static String findLonKeyByHeader(Map<String, Integer> headers) {
        if (headers.containsKey("lon")) {
            return "lon";
        }
        if (headers.containsKey("longitude")) {
            return "longitude";
        }
        return "";
    }
}
