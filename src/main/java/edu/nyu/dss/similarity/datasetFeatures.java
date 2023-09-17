package edu.nyu.dss.similarity;

import edu.rmit.trajectory.clustering.kmeans.IndexAlgorithm;
import edu.rmit.trajectory.clustering.kmeans.indexNode;
import edu.rmit.trajectory.clustering.kpaths.Util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Set;

public class datasetFeatures {
	
	/*
	 * in this class, we extract featurs of index which can distinguish dataset 
	 */
	
	static IndexAlgorithm indexkmeans = new IndexAlgorithm();
	
	static double feature[];
	
	/*
	 * get the count of nodes in the tree.
	 */
	static public int getNodesCount(indexNode root) {
		if(root == null)
			return 0;
		if(root.isLeaf()) {	
			return 1;
		}else {
			Set<indexNode> listnode = root.getNodelist();
			int max = listnode.size();
			for(indexNode aIndexNode: listnode) {
				max += getNodesCount(aIndexNode);
			}
			return max;
		}
	}
	
	
	/*
	 * the features of index tree, which will be used, we need to solve the N/A problem
	 */
	static public double[] getFeature(String filename, indexNode root, int scale, int capacity, int id) {
		ArrayList<Double> raidus = new ArrayList<Double>();
		ArrayList<Double> fatherdis = new ArrayList<Double>();
		ArrayList<Double> numPoints = new ArrayList<Double>();
		ArrayList<Double> nodeDepth = new ArrayList<Double>();
		
		int feature_number = 12;
		feature = new double[feature_number];
		double leafnode = indexkmeans.getLeafRadius(root, raidus, root.getRadius(),fatherdis, numPoints, nodeDepth, 0)/(scale/(double)capacity);

		DecimalFormat dec = new DecimalFormat("#0.0000");
		double mean = Util.calculateMean(raidus);//
		
		double depth_mean = Util.calculateMean(nodeDepth);//
		double depth_sd = Util.calculateSD(nodeDepth, depth_mean);//
		
		double coverPointsMean = Util.calculateMean(numPoints);//
		double coverPointsSD = Util.calculateSD(numPoints, coverPointsMean);//
		
		double disFatherMean = Util.calculateMean(fatherdis);//
		double disFatherSD = Util.calculateSD(fatherdis, disFatherMean);//
		
		int expectedHeight = (int) (Math.log(scale/(float)capacity) / Math.log(2.0));
		String content = id+",";
		double pivot[] = root.getPivot();
		for(double p: pivot)
			content += p+",";
		content+= dec.format(root.getRadius())+",";
		
		content += scale+","+dec.format(indexkmeans.getHeight(root)/(float)expectedHeight)+","+dec.format(getNodesCount(root)/(scale/(double)capacity))+
				","+dec.format(leafnode)+","+dec.format(depth_mean/expectedHeight)+","+dec.format(depth_sd/expectedHeight)+","
				+dec.format(mean)+","+dec.format(Util.calculateSD(raidus, mean))+","+
				dec.format(coverPointsMean/(double)capacity)+","+dec.format(coverPointsSD/(double)capacity)+","+dec.format(disFatherMean)+","+dec.format(disFatherSD);
		Util.write(filename, content+"\n");
		
		feature[0] = (double)scale;
		feature[1] = indexkmeans.getHeight(root)/(float)expectedHeight;
		feature[2] = getNodesCount(root)/(scale/(double)capacity);
		feature[3] = leafnode;
		feature[4] = depth_mean/expectedHeight;
		feature[5] = depth_sd/expectedHeight;
		feature[6] = mean;
		feature[7] = Util.calculateSD(raidus, mean);
		feature[8] = coverPointsMean/capacity;
		feature[9] = coverPointsSD/capacity;
		feature[10] = disFatherMean;
		feature[11] = disFatherSD;
		
		// add more features and try to group them for a compact, pca analysis to build relationship
		//Process p = Runtime.getRuntime().exec("python yourapp.py");
		return feature;
	}
	
	void ProjectedFeatureIndexing() {
		// we read the projected features from file, and 
	}
}
