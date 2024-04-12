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
    @Query("select d from Delivery d left join d.order o left join d.courier c left join d.status s left join o.client cl left join o.orderStatus st  left join o.paymentType p left join o.receiveType r left join o.worker w")
    Page<Delivery> findAll(Pageable pageable);
    @Query("select d from Delivery d left join d.order o left join d.courier c left join d.status s left join o.client cl left join o.orderStatus st  left join o.paymentType p left join o.receiveType r left join o.worker w where d.id = ?1")
    Delivery findDeliveryById(Long id);
}
