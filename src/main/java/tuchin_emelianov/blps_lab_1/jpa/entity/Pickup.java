package tuchin_emelianov.blps_lab_1.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/*
Номер заказа
Работник
Магазин
Статус
Срок хранения
*/
@Entity
@Getter
@Setter
public class Pickup {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Human worker;

    @ManyToOne
    private ReceiveStatus receiveStatus;

    private Date date;

    @OneToOne
    private Orders order;
}
