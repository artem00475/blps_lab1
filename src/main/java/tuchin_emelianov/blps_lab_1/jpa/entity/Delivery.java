package tuchin_emelianov.blps_lab_1.jpa.entity;

/*
Номер заказа
Курьер
Адрес
Дата доставки
Статус
*/

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
public class Delivery {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    private Orders order;

    @ManyToOne
    private Human courier;

    private String address;

    private Date date;

    @ManyToOne
    private DeliveryStatus status;

}
