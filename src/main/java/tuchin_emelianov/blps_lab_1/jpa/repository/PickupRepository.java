package tuchin_emelianov.blps_lab_1.jpa.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tuchin_emelianov.blps_lab_1.jpa.entity.Orders;
import tuchin_emelianov.blps_lab_1.jpa.entity.Pickup;
import tuchin_emelianov.blps_lab_1.jpa.entity.ReceiveStatus;
import java.util.Date;

public interface PickupRepository extends JpaRepository<Pickup,Long> {
   Pickup findPickupByOrder (Orders order);
   Pickup findAllByReceiveStatus (ReceiveStatus status);
   Pickup findAllByDate (Date date);
   boolean existsByOrder(Orders order);
   @Query("select p from Pickup p join fetch p.worker w join fetch p.order o join fetch p.receiveStatus s join fetch o.worker w1 join fetch o.client c join fetch o.orderStatus st join fetch o.paymentType pt join fetch o.receiveType rt")
   Page<Pickup> findAll(Pageable pageable);
   @Query("select p from Pickup p join fetch p.worker w join fetch p.order o join fetch p.receiveStatus s join fetch o.worker w1 join fetch o.client c join fetch o.orderStatus st join fetch o.paymentType pt join fetch o.receiveType rt where p.id = ?1")
   Pickup findPickupById(Long id);
}