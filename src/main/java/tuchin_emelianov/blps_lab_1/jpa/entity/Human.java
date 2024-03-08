package tuchin_emelianov.blps_lab_1.jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/*
* ФИО
* Телефон
* Почта
* Дата регистрации
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

    private Date date;

    private String role;
}
