package tuchin_emelianov.blps_lab_1.jpa.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import tuchin_emelianov.blps_lab_1.jpa.entity.PaymentType;

public interface PaymentTypeRepository extends JpaRepository<PaymentType,Long> {
   PaymentType findByType (String type);
}