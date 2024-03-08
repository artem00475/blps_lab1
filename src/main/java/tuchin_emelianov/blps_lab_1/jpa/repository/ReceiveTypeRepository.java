package tuchin_emelianov.blps_lab_1.jpa.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import tuchin_emelianov.blps_lab_1.jpa.entity.ReceiveType;

public interface ReceiveTypeRepository extends JpaRepository<ReceiveType,Long> {
   ReceiveType findByType (String type);
}
