package web.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@Entity(name = "data_order")
public class Order {
    @Id
    @Column(name = "order_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "dataset_ids")
    private String datasetIds;

    @Column(name = "dataset_cnt")
    private Integer datasetCnt;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "is_paid")
    private Boolean isPaid;

    @Column(name = "create_time")
    private Timestamp createTime;
}
