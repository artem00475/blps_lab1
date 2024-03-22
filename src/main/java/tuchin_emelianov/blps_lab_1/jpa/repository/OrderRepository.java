package tuchin_emelianov.blps_lab_1.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tuchin_emelianov.blps_lab_1.jpa.entity.Human;
import tuchin_emelianov.blps_lab_1.jpa.entity.OrderStatus;
import tuchin_emelianov.blps_lab_1.jpa.entity.Orders;

public interface OrderRepository extends JpaRepository<Orders, Long> {
    @Query("select o from Orders o left join o.worker w left join o.receiveType r left join o.paymentType p join fetch o.orderStatus s join fetch o.client c where o.id = ?1")
    Orders findOrderById (Long id);
    Orders findOrderByOrderStatus (OrderStatus orderStatus);
    Orders findOrderByClient (Human client);
    Orders findOrderByWorker (Human worker);
    @Query("select o from Orders o left join o.worker w left join o.receiveType r left join o.paymentType p join fetch o.orderStatus s join fetch o.client c")
    Page<Orders> findAll(Pageable pageable);

}
