package tuchin_emelianov.blps_lab_1.jpa.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import tuchin_emelianov.blps_lab_1.jpa.entity.Orders;
import tuchin_emelianov.blps_lab_1.jpa.entity.Pickup;
import tuchin_emelianov.blps_lab_1.jpa.entity.ReceiveStatus;
import java.util.Date;

public interface PickupRepository extends JpaRepository<Pickup,Long> {
   Pickup findPickupByOrder (Orders order);
   Pickup findAllByReceiveStatus (ReceiveStatus status);
   Pickup findAllByDate (Date date);
   boolean existsByOrder(Orders order);

}