package tuchin_emelianov.blps_lab_1.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tuchin_emelianov.blps_lab_1.jpa.entity.Human;
import tuchin_emelianov.blps_lab_1.jpa.entity.User;
import tuchin_emelianov.blps_lab_1.jpa.repository.HumanRepository;
import tuchin_emelianov.blps_lab_1.request.SignUpRequest;

@Service
public class HumanService {

    private final HumanRepository humanRepository;
    private final UserService userService;
    private final TransactionTemplate transactionTemplate;

    public HumanService(HumanRepository humanRepository, UserService userService, PlatformTransactionManager platformTransactionManager) {
        this.humanRepository = humanRepository;
        this.userService = userService;
        this.transactionTemplate = new TransactionTemplate(platformTransactionManager);
    }


//    public boolean checkUser(Long id) {
//        return !humanRepository.existsById(id);
//    }

    public Human getHumanByUser(User user) {
        return humanRepository.findByUser(user);
    }

    public Human addUser(String fio, String mail, String phone, String username, String password, String role) {
        transactionTemplate.setIsolationLevelName("ISOLATION_REPEATABLE_READ");
        return transactionTemplate.execute(status -> {
            userService.existsUser(username);
            Human human = new Human();
            human.setFio(fio);
            human.setMail(mail);
            human.setPhone(phone);
            human.setUser(userService.createUser(username, password, role));
            human = humanRepository.save(human);

            return human;
        });
    }

    public Human signUp(SignUpRequest signUpRequest) {
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
