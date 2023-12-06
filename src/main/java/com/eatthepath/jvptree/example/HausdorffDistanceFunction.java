package com.eatthepath.jvptree.example;

import com.eatthepath.jvptree.DistanceFunction;

import edu.rmit.trajectory.clustering.kmeans.IndexNode;

public class HausdorffDistanceFunction implements DistanceFunction<IndexNode>{
	public double getDistance(final IndexNode firstPoint, final IndexNode secondPoint) {
		// using the approximate index, 
		
		// call the approximate distance, can the error be accumulated?
		
		// should be fine, without changing, only for the search, 
        return 0;
    }
}
