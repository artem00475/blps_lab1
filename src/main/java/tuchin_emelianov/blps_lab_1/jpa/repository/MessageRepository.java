package tuchin_emelianov.blps_lab_1.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tuchin_emelianov.blps_lab_1.jpa.entity.Human;
import tuchin_emelianov.blps_lab_1.jpa.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findMessagesByReceiver(Human human, Pageable pageable);
    Message findMessagesByReceiverAndId(Human human, Long id);
}
