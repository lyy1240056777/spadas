package edu.nyu.dss.similarity.index;

import edu.rmit.trajectory.clustering.kmeans.indexNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class IndexMap extends HashMap<Integer, indexNode> {
}
