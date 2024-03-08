package tuchin_emelianov.blps_lab_1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tuchin_emelianov.blps_lab_1.jpa.entity.Human;
import tuchin_emelianov.blps_lab_1.jpa.repository.HumanRepository;

import java.util.Date;

@Service
public class HumanService {
    @Autowired
    private HumanRepository humanRepository;

    public boolean checkUser(Long id) {
        return !humanRepository.existsByIdAndRole(id, "Клиент");
    }

    public boolean checkWorker(Long id) {
        return !humanRepository.existsByIdAndRole(id, "Работник");
    }

    public boolean checkCourier(Long id) {
        return !humanRepository.existsByIdAndRole(id, "Курьер");
    }

    public Human getUser(Long id) {
        return humanRepository.findHumanById(id);
    }

    public Long addUser(String fio, String mail, String phone) {
        if (fio.isEmpty() || mail.isEmpty() || phone.isEmpty()) {
            return 0L;
        }
        Human human = new Human();
        human.setFio(fio);
        human.setMail(mail);
        human.setPhone(phone);
        human.setDate(new Date());
        human.setRole("Клиент");
        human = humanRepository.save(human);
        return human.getId();
    }
}
