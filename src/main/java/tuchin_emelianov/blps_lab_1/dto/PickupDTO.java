package tuchin_emelianov.blps_lab_1.dto;

import lombok.Data;
import tuchin_emelianov.blps_lab_1.jpa.entity.ReceiveStatus;

import java.util.Date;

@Data
public class PickupDTO {
    private Long id;

    private HumanDTO worker;

    private ReceiveStatus receiveStatus;

    private Date date;

    private OrderDTO order;
}
