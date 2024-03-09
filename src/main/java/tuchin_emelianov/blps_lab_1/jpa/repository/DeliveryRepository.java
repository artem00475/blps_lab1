package tuchin_emelianov.blps_lab_1.jpa.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import tuchin_emelianov.blps_lab_1.jpa.entity.Delivery;
import tuchin_emelianov.blps_lab_1.jpa.entity.Orders;

import java.util.Date;


public interface DeliveryRepository extends JpaRepository<Delivery,Long>{
    Delivery findAllByDate (Date date);
    Delivery findAllByAddress (String address);
    Delivery findDeliveryByOrder (Orders order);
    boolean existsByOrder(Orders order);
    @Query("select d from Delivery d join fetch d.order o join fetch d.courier c join fetch d.status s join fetch o.client cl join fetch o.orderStatus st  join fetch o.paymentType p join fetch o.receiveType r join fetch o.worker w")
    Page<Delivery> findAll(Pageable pageable);
    @Query("select d from Delivery d join fetch d.order o join fetch d.courier c join fetch d.status s join fetch o.client cl join fetch o.orderStatus st  join fetch o.paymentType p join fetch o.receiveType r join fetch o.worker w where d.id = ?1")
    Delivery findDeliveryById(Long id);
}
