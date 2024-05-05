package web.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderVO {
    private int orderId;

    private String createTime;

    private List<DatasetVo> datasets;

    private BigDecimal totalPrice;

//    需要被序列化的类的字段不能以"is"开头，否则"is"会被省略
    private boolean paid;

    private int datasetCnt;
}
