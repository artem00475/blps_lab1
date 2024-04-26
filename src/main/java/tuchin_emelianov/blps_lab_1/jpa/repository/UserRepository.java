package tuchin_emelianov.blps_lab_1.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tuchin_emelianov.blps_lab_1.jpa.entity.Role;
import tuchin_emelianov.blps_lab_1.jpa.entity.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    boolean existsByUsername(String username);
    List<User> findUsersByRoles(Role role);
}
