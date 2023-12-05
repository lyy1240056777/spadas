package edu.nyu.dss.similarity;

import emd.relaxIndexNode;

import java.util.Comparator;

public class ComparatorByRelaxIndexNode implements Comparator { //small->large
    public int compare(Object o1, Object o2) {
        relaxIndexNode in1 = (relaxIndexNode) o1;
        relaxIndexNode in2 = (relaxIndexNode) o2;
        return (in2.getLb() - in1.getLb() > 0) ? 1 : -1;
    }
}
