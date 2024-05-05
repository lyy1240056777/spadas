package web.Repo;

import org.springframework.data.jpa.repository.JpaRepository;
import web.entity.Order;

import java.util.List;

public interface OrderRepo extends JpaRepository<Order, Integer> {
    Order findByOrderId(int orderId);

    int countByIsPaid(boolean isPaid);

    Order findByDatasetIds(String string);

    List<Order> findAllByUserId(int userId);

    void deleteByOrderId(int orderId);
}
