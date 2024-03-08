package tuchin_emelianov.blps_lab_1.jpa.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import tuchin_emelianov.blps_lab_1.jpa.entity.ReceiveStatus;

public interface ReceiveStatusRepository extends JpaRepository<ReceiveStatus,Long> {
   ReceiveStatus findByType (String type);

}