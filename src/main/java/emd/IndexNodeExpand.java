package emd;

import edu.rmit.trajectory.clustering.kmeans.IndexNode;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IndexNodeExpand {
    public IndexNode in;
    public double lb;
    public double ub;

    public IndexNodeExpand(IndexNode index) {
        this.in = index;
    }

    public IndexNodeExpand(IndexNode index, double lb) {
        this.in = index;
        this.lb = lb;
    }
}
