package tuchin_emelianov.blps_lab_1.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/*
* ФИО
* Телефон
* Почта
*/
@Entity
@Getter
@Setter
public class Human {

    @Id
    @GeneratedValue
    private Long id;

    private String fio;

    private String phone;

    private String mail;

    @OneToOne(fetch = FetchType.EAGER)
    private User user;
}
