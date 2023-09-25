package edu.nyu.dss.similarity.statistics;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
public class PointCounter {

    private int count;

    public void clear() {
        this.count = 0;
    }

    public void put(int i) {
        count += i;
    }

    public int get() {
        return count;
    }
}
