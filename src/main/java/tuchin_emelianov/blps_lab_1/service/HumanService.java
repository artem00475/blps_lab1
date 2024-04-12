package tuchin_emelianov.blps_lab_1.service;

import com.atomikos.icatch.jta.UserTransactionImp;
import jakarta.transaction.SystemException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tuchin_emelianov.blps_lab_1.jpa.entity.Human;
import tuchin_emelianov.blps_lab_1.jpa.entity.User;
import tuchin_emelianov.blps_lab_1.jpa.repository.HumanRepository;
import tuchin_emelianov.blps_lab_1.request.SignUpRequest;

@Service
@AllArgsConstructor
public class HumanService {

    private final HumanRepository humanRepository;
    private final UserService userService;
    private final UserTransactionImp utx;

    public boolean checkUser(Long id) {
        return !humanRepository.existsById(id);
    }

    public Human getHumanByUser(User user) {
        return humanRepository.findByUser(user);
    }

    public Human addUser(String fio, String mail, String phone, String username, String password, String role) throws SystemException {
        Human human = new Human();
        human.setFio(fio);
        human.setMail(mail);
        human.setPhone(phone);
        try {
            utx.begin();
            human.setUser(userService.createUser(username, password, role));
            human = humanRepository.save(human);
            utx.commit();
        } catch (Exception e) {
            utx.rollback();
        }
        return human;
    }

    public Human signUp(SignUpRequest signUpRequest) throws SystemException {
        return addUser(
                signUpRequest.getFio(),
                signUpRequest.getMail(),
                signUpRequest.getPhone(),
                signUpRequest.getUsername(),
                signUpRequest.getPassword(),
                signUpRequest.getRole()
        );
    }
}
