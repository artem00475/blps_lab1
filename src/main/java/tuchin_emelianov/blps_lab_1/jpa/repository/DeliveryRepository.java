package tuchin_emelianov.blps_lab_1.jpa.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import tuchin_emelianov.blps_lab_1.jpa.entity.Delivery;
import tuchin_emelianov.blps_lab_1.jpa.entity.Orders;

import java.util.Date;


public interface DeliveryRepository extends JpaRepository<Delivery,Long>{
    Delivery findAllByDate (Date date);
    Delivery findAllByAddress (String address);
    Delivery findDeliveryByOrder (Orders order);
    boolean existsByOrder(Orders order);
}
