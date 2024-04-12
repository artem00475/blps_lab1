package tuchin_emelianov.blps_lab_1.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tuchin_emelianov.blps_lab_1.jpa.entity.Human;
import tuchin_emelianov.blps_lab_1.jpa.entity.User;

public interface HumanRepository extends JpaRepository<Human, Long> {
    Human findHumanById(Long id);
    Human findHumanByFio(String fio);
    Human findHumanByMail(String mail);
    Human findHumanByPhone(String phone);
    Human findByUser(User user);
}
