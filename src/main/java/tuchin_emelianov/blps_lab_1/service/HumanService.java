package tuchin_emelianov.blps_lab_1.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tuchin_emelianov.blps_lab_1.jpa.entity.Human;
import tuchin_emelianov.blps_lab_1.jpa.repository.HumanRepository;

import java.util.Date;

@Service
@AllArgsConstructor
public class HumanService {

    private final HumanRepository humanRepository;
    private final UserService userService;

    public boolean checkUser(Long id) {
        return !humanRepository.existsById(id);
    }

    public Human getUser(Long id) {
        return humanRepository.findHumanById(id);
    }

    public Long addUser(String fio, String mail, String phone, String username, String password) {
        if (fio.isEmpty() || mail.isEmpty() || phone.isEmpty() || username.isEmpty() || password.isEmpty()) {
            return 0L;
        }
        Human human = new Human();
        human.setFio(fio);
        human.setMail(mail);
        human.setPhone(phone);
        human.setUser(userService.createUser(username, password, "Клиент"));
        human = humanRepository.save(human);
        return human.getId();
    }
}
