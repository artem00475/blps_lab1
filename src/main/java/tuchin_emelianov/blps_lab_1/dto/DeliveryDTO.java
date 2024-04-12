package tuchin_emelianov.blps_lab_1.dto;

import lombok.Data;
import tuchin_emelianov.blps_lab_1.jpa.entity.DeliveryStatus;
import tuchin_emelianov.blps_lab_1.jpa.entity.Human;
import tuchin_emelianov.blps_lab_1.jpa.entity.Orders;

import java.util.Date;

@Data
public class DeliveryDTO {

    private Long id;

    private OrderDTO order;

    private HumanDTO courier;

    private String address;

    private Date date;

    private DeliveryStatus status;
}
