package web.VO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JoinPair {

    private JoinPoint queryPoint;

    private JoinPoint targetPoint;

    private double distance;
}
