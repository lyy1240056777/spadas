package edu.nyu.dss.similarity;


import java.util.PriorityQueue;

import edu.rmit.trajectory.clustering.kmeans.IndexNode;

/*
 * this class is for the heap method
 */
public class queueMain implements Comparable<queueMain>{
	double ub;
	IndexNode anode;
	int pointid;
	PriorityQueue<queueSecond> aSecond;

	
	public double getbound() {
		return ub;
	}
	
	public IndexNode getIndexNode() {
		return anode;
	}
	
	public int getpointID() {
		return pointid;
	}
	
	public PriorityQueue<queueSecond> getQueue(){
		return aSecond;
	}
	
	public queueMain(IndexNode a, PriorityQueue<queueSecond> bSecond, double ub, int pointid) {
		this.anode = a;
		this.aSecond = bSecond;
		this.ub = ub;
		this.pointid = pointid;
	}
	
	@Override
    public int compareTo(queueMain other) {
		double gap = this.getbound() - other.getbound();
		
		if(gap>0)
			return -1;
		else if(gap==0)
			return 0;
		else {
			return 1;
		}
    }
	
}
