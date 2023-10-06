package web.param;

import lombok.Data;

@Data
public class AugmentParams {
    /**
     * 查找的指定数据集 ID
     */
    private int datasetID;

    /**
     * how many dataset should we return(same as column number augmented)
     */
    private int datasetCount;

    /**
     * how many items should we search for each data point
     */
    private int candidateCount;

    private DatasetQueryParams options;
}
