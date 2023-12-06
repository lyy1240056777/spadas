package edu.nyu.dss.similarity.statistics;

import org.springframework.stereotype.Component;

import java.util.TreeMap;


@Component
/**
 * generate the Count Histogram for all datasets
 */
public class DatasetSizeCounter {

    private TreeMap<Integer, Integer> map;

    public DatasetSizeCounter() {
        this.map = new TreeMap<>();
    }

    public void clear() {
        this.map = new TreeMap<>();
    }

    public void put(int i) {
        if (map.containsKey(i))
            map.put(i, map.get(i) + 1);
        else
            map.put(i, 1);
    }

    public TreeMap<Integer, Integer> get() {
        return map;
    }


}
