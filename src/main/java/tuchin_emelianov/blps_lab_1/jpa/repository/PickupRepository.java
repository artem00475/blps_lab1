package tuchin_emelianov.blps_lab_1.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tuchin_emelianov.blps_lab_1.jpa.entity.Orders;
import tuchin_emelianov.blps_lab_1.jpa.entity.Pickup;
import tuchin_emelianov.blps_lab_1.jpa.entity.ReceiveStatus;
import java.util.Date;
import java.util.List;

public interface PickupRepository extends JpaRepository<Pickup,Long> {
   Pickup findPickupByOrder (Orders order);
   Pickup findAllByReceiveStatus (ReceiveStatus status);
   Pickup findAllByDate (Date date);
   boolean existsByOrder(Orders order);
   @Query("select p from Pickup p left join p.worker w left join p.order o left join p.receiveStatus s left join o.worker w1 left join o.client c left join o.orderStatus st left join o.paymentType pt left join o.receiveType rt")
   Page<Pickup> findAll(Pageable pageable);
   @Query("select p from Pickup p left join p.worker w left join p.order o left join p.receiveStatus s left join o.worker w1 left join o.client c left join o.orderStatus st left join o.paymentType pt left join o.receiveType rt where p.id = ?1")
   Pickup findPickupById(Long id);
   List<Pickup> findAllByDateLessThanAndReceiveStatus(Date dateTime, ReceiveStatus status);
}