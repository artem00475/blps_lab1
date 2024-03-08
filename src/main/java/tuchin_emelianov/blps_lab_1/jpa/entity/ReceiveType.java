package tuchin_emelianov.blps_lab_1.jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

/*
Тип
Минимальная сумма
*/
@Entity
@Getter
@Setter
public class ReceiveType {

    @Id
    @GeneratedValue
    private Long id;

    private String type;

    private int minSum;
}
