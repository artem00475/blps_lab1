package tuchin_emelianov.blps_lab_1.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tuchin_emelianov.blps_lab_1.jpa.entity.Human;

public interface HumanRepository extends JpaRepository<Human, Long> {
    Human findHumanById(Long id);
    Human findHumanByFio(String fio);
    Human findHumanByMail(String mail);
    Human findHumanByPhone(String phone);
    boolean existsByIdAndRole(Long id, String role);
}
