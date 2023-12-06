package edu.nyu.dss.similarity;

import emd.IndexNodeExpand;

import java.util.Comparator;

public class ComparatorByIndexNodeExpand implements Comparator { //ordered by distance
    public int compare(Object o1, Object o2) {
        IndexNodeExpand in1 = (IndexNodeExpand) o1;
        IndexNodeExpand in2 = (IndexNodeExpand) o2;
        return (in2.getLb() - in1.getLb() > 0) ? 1 : -1;
    }
}
