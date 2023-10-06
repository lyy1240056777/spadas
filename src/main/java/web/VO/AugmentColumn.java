package web.VO;

import lombok.Data;

import java.util.List;

@Data
public class AugmentColumn<T> {
    private int id;

    private String name;

    /**
     * where this column from
     */
    private int datasetID;

    /**
     * the actual values of this column
     */
    private List<T> values;

    /**
     * is this column a column extended from origin?
     */
    private boolean isAugment;
}
