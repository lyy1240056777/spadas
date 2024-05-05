package web.param;

import lombok.Data;

import java.util.List;

@Data
public class OrderParams {
    List<Integer> datasets;
    double totalPrice;
}
