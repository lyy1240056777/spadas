package edu.nyu.dss.similarity.index;

import edu.rmit.trajectory.clustering.kmeans.IndexNode;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class IndexMap extends HashMap<Integer, IndexNode> {
}
