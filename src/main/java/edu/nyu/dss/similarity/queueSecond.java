package edu.nyu.dss.similarity;

import edu.rmit.trajectory.clustering.kmeans.IndexNode;

/*
 * this class is for the heap method
 */
public class queueSecond implements Comparable<queueSecond>{
	double bound;
	IndexNode anode;
	int pointid;

	
	public double getbound() {
		return bound;
	}
	
	public IndexNode getNode() {
		return anode;
	}
	
	public int getPointId() {
		return pointid;
	}
	
	public queueSecond(IndexNode a, double b, int id) {
		this.anode = a;
		this.bound = b;
		this.pointid = id;
	}
	
	@Override
    public int compareTo(queueSecond other) {
		double gap = this.getbound() - other.getbound();
		
		if(gap>0)
			return 1;
		else if(gap==0)
			return 0;
		else {
			return -1;
		}
    }
	
}
