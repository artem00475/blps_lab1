package tuchin_emelianov.blps_lab_1.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tuchin_emelianov.blps_lab_1.jpa.entity.Human;
import tuchin_emelianov.blps_lab_1.jpa.entity.OrderStatus;
import tuchin_emelianov.blps_lab_1.jpa.entity.Orders;

public interface OrderRepository extends JpaRepository<Orders, Long> {
    Orders findOrderById (Long id);
    Orders findOrderByOrderStatus (OrderStatus orderStatus);
    Orders findOrderByClient (Human client);
    Orders findOrderByWorker (Human worker);

}
