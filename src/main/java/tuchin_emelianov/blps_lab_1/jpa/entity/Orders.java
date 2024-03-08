package tuchin_emelianov.blps_lab_1.jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/*
Номер заказа
Время создания
Время последнего статуса
Способ оплаты
Статус
Клиент
Работник
Способ получения
*/
@Entity
@Getter
@Setter
public class Orders {

    @Id
    @GeneratedValue
    private Long id;

    private Date date;

    private Date lastStatusDate;

    @ManyToOne
    private PaymentType paymentType;

    @ManyToOne
    private ReceiveType receiveType;

    @ManyToOne
    private OrderStatus orderStatus;

    @ManyToOne
    private Human client;

    @ManyToOne
    private Human worker;
}
