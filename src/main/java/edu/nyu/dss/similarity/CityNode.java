package edu.nyu.dss.similarity;

import edu.rmit.trajectory.clustering.kmeans.IndexNode;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//a temporary data structure for demo,
//to show a city node before showing all the dataset nodes
public class CityNode {
    public String cityName;
    @JsonIgnore
    public List<IndexNode> nodeList;
    public int nodeCount;

    public double[] pivot;

    public double radius;
    public double[] maxBox;
    public double[] minBox;

    public CityNode(String cityName, int dimension) {
        this.cityName = cityName;
        this.nodeList = new ArrayList<>();
        Framework.cityIndexNodeMap.put(cityName, new ArrayList<>());
        this.nodeCount = 0;
        pivot = new double[dimension];
        Arrays.fill(pivot, 0);
        radius = 0;
        maxBox = new double[dimension];
        minBox = new double[dimension];
    }

    public CityNode(String cityName, List<IndexNode> nodeList, int dimension) {
        this.cityName = cityName;
        this.nodeList = nodeList;
        this.nodeCount = nodeList.size();
        pivot = new double[dimension];
        Arrays.fill(pivot, 0);
        for (int i = 0; i < dimension; i++) {
            for (IndexNode node : nodeList) {
                pivot[i] += node.getPivot()[i];
            }
            pivot[i] /= nodeList.size();
        }
    }

    public void calPivot(int dimension) {
        for (int i = 0; i < dimension; i++) {
            for (IndexNode node : nodeList) {
                pivot[i] += node.getPivot()[i];
            }
            pivot[i] /= nodeList.size();
        }
    }

    public void calAttrs(int dimension) {
        for (int i = 0; i < dimension; i++) {
            maxBox[i] = -Double.MAX_VALUE;
            minBox[i] = Double.MAX_VALUE;
            for (IndexNode node : nodeList) {
                maxBox[i] = Math.max(maxBox[i], node.getMBRmax()[i]);
                minBox[i] = Math.min(minBox[i], node.getMBRmin()[i]);
            }
            pivot[i] = (maxBox[i] + minBox[i]) / 2;
            radius += Math.pow((maxBox[i] - minBox[i]) / 2, 2);
        }
        radius = Math.sqrt(radius);
    }
}
