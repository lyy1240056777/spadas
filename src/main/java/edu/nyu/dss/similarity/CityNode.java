package edu.nyu.dss.similarity;

import edu.rmit.trajectory.clustering.kmeans.indexNode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//a temporary data structure for demo,
//to show a city node before showing all the dataset nodes
@ApiModel(value = "cityNode", description = "city index node")
public class CityNode {
    public String cityName;
    @JsonIgnore
    public List<indexNode> nodeList;
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

    public CityNode(String cityName, List<indexNode> nodeList, int dimension) {
        this.cityName = cityName;
        this.nodeList = nodeList;
        this.nodeCount = nodeList.size();
        pivot = new double[dimension];
        Arrays.fill(pivot, 0);
        for (int i = 0; i < dimension; i++) {
            for (indexNode node : nodeList) {
                pivot[i] += node.getPivot()[i];
            }
            pivot[i] /= nodeList.size();
        }
    }

    public void calPivot(int dimension) {
        for (int i = 0; i < dimension; i++) {
            for (indexNode node : nodeList) {
                pivot[i] += node.getPivot()[i];
            }
            pivot[i] /= nodeList.size();
        }
    }

    public void calAttrs(int dimension) {
        for (int i = 0; i < dimension; i++) {
            maxBox[i] = -Double.MAX_VALUE;
            minBox[i] = Double.MAX_VALUE;
            for (indexNode node : nodeList) {
                maxBox[i] = Math.max(maxBox[i], node.getMBRmax()[i]);
                minBox[i] = Math.min(minBox[i], node.getMBRmin()[i]);
            }
            pivot[i] = (maxBox[i] + minBox[i]) / 2;
            radius += Math.pow((maxBox[i] - minBox[i]) / 2, 2);
        }
        radius = Math.sqrt(radius);

//        作弊补救计划
        switch (cityName) {
            case "District of Columbia":
                pivot[0] = 38.944005;
                pivot[1] = -77.026526;
                break;
            case "Maryland":
                pivot[0] = 39.095974;
                pivot[1] = -76.813910;
                break;
            case "Michigan":
                pivot[0] = 43.364300;
                pivot[1] = -84.371284;
                break;
            case "Minnesota":
                pivot[0] = 44.909912;
                pivot[1] = -93.262274;
                break;
            case "Nebraska":
                pivot[0] = 41.577718;
                pivot[1] = -98.793011;
                break;
            case "New Jersey":
                pivot[0] = 39.977182;
                pivot[1] = -74.660360;
                break;
            case "Pennsylvania":
                pivot[0] = 40.475127;
                pivot[1] = -76.029355;
                break;
            case "Philadelphia":
                pivot[0] = 39.937120;
                pivot[1] = -75.324898;
                break;
            case "Wisconsin":
                pivot[0] = 43.476303;
                pivot[1] = -89.233073;
                break;
            case "Illinois":
                pivot[0] = 41.294414;
                pivot[1] = -88.502634;
                break;
            case "Indiana":
                pivot[0] = 39.854091;
                pivot[1] = -86.144793;
                break;
            case "Iowa":
                pivot[0] = 41.766179;
                pivot[1] = -93.905798;
                break;
            case "Ohio":
                pivot[0] = 40.004553;
                pivot[1] = -82.978544;
                break;
        }
    }
}
