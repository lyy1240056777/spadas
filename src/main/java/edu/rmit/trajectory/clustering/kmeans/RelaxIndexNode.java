package edu.rmit.trajectory.clustering.kmeans;

import lombok.Data;

@Data
public class RelaxIndexNode {
    public int id;
    public double lb;
    public double ub;

    public RelaxIndexNode(int id, double lb, double ub) {
        this.id = id;
        this.lb = lb;
        this.ub = ub;
    }
}
