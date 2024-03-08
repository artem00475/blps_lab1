package tuchin_emelianov.blps_lab_1.jpa.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import tuchin_emelianov.blps_lab_1.jpa.entity.DeliveryStatus;

public interface DeliveryStatusRepository extends JpaRepository<DeliveryStatus,Long> {
   DeliveryStatus findByType (String type);
}