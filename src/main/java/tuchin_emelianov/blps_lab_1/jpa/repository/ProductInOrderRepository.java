package tuchin_emelianov.blps_lab_1.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tuchin_emelianov.blps_lab_1.jpa.entity.Orders;
import tuchin_emelianov.blps_lab_1.jpa.entity.ProductInOrder;

import java.util.List;

public interface ProductInOrderRepository extends JpaRepository<ProductInOrder, Long> {
    List<ProductInOrder> findAllProductInOrderByOrders (Orders order);
}
