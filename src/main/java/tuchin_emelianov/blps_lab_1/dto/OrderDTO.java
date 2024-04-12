package tuchin_emelianov.blps_lab_1.dto;

import lombok.Data;
import lombok.ToString;
import tuchin_emelianov.blps_lab_1.jpa.entity.OrderStatus;
import tuchin_emelianov.blps_lab_1.jpa.entity.PaymentType;
import tuchin_emelianov.blps_lab_1.jpa.entity.ReceiveType;

import java.util.Date;
@Data
public class OrderDTO {

    private Long id;

    private Date date;

    private Date lastStatusDate;

    private PaymentType paymentType;

    private ReceiveType receiveType;

    private OrderStatus orderStatus;

    private HumanDTO client;

    private HumanDTO worker;
}
