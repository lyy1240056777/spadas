package edu.whu.tmeans.augment;

import edu.whu.tmeans.model.GeoLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BrutalForceAugment {
    /**
     * find the nearest points in candidateDataset with brutal force method.
     *
     * @param originDataset    target dataset
     * @param candidateDataset candidate dataset
     * @param num              nearest top k
     * @return list for each point
     */
    public static List<GeoLocation[]> getNearestPoints(List<GeoLocation> originDataset, List<GeoLocation> candidateDataset, int num) {
        List<GeoLocation[]> result = new ArrayList<>();
        for (GeoLocation center : originDataset) {
            List<GeoLocation> tops = candidateDataset.stream().sorted(Comparator.comparing(a -> a.distance(center))).limit(num).toList();
            result.add(tops.toArray(new GeoLocation[0]));
        }
        return result;
    }
}
