package edu.nyu.dss.similarity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.rmit.trajectory.clustering.kpaths.Util;

public class trajectoryProvanance {
	
	/**/
	void outlier() {
		//run outlier removal
	}
	
	void segmentation() {
		
	}
	
	void clean_raw() {
		
	}
	
	void convertVertex(String vertexfile) {
		
	}
	
	static void convertEdge(String edgefile, String edgeTrajectory, String writeedge, String writevertex) throws FileNotFoundException, IOException {
		File file = new File(edgefile);
		Map<Integer, String> edgefileMap = new HashMap<Integer, String>();
		Map<Integer, String> vertexfileMap = new HashMap<Integer, String>();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] splitString = strLine.split(";");
				String[] edgeidStrings = splitString[1].split(",");
				String[] edgeidStrings1 = splitString[2].split(",");
				String edgeString = "";
				System.out.println(splitString[1]);
				for(int i =0; i<edgeidStrings.length-1; i++) {
					edgeString += edgeidStrings1[i]+","+edgeidStrings[i]+",";
					System.out.println(edgeidStrings1[i]+","+edgeidStrings[i]);
				}
				String vertexString = edgeidStrings1[0]+","+edgeidStrings[0];
				edgefileMap.put(Integer.valueOf(splitString[0]), edgeString);
				vertexfileMap.put(Integer.valueOf(splitString[0]), vertexString);
			}
		}
		file = new File(edgeTrajectory);
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] splitString = strLine.split("\t");
				String[] edgeidStrings = splitString[1].split(",");
				String edgeString = "";
				String vertexString = "";
				for(int i = 0; i<edgeidStrings.length; i++) {
					int edgeid = Integer.valueOf(edgeidStrings[i]);
					edgeString += edgefileMap.get(edgeid);
					vertexString += vertexfileMap.get(edgeid)+",";
				}
				Util.write(writeedge, edgeString+"\n");
				Util.write(writevertex, vertexString+"\n");
			}
		}
	}
	

}
