package tuchin_emelianov.blps_lab_1.dto;

import lombok.Data;

@Data
public class NotificationDTO {
    private Long id;

    private String object;

    private String content;
}
