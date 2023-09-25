package emd;

import edu.rmit.trajectory.clustering.kmeans.indexNode;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IndexNodeExpand {
    public indexNode in;
    public double lb;
    public double ub;

    public IndexNodeExpand(indexNode index) {
        this.in = index;
    }

    public IndexNodeExpand(indexNode index, double lb) {
        this.in = index;
        this.lb = lb;
    }
}
