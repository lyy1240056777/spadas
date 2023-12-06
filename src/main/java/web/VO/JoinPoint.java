package web.VO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JoinPoint {
    private int id;

    private double[] location;
}
