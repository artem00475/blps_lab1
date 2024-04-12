package tuchin_emelianov.blps_lab_1.dto;

import lombok.Data;

@Data
public class HumanDTO {
    private Long id;

    private String fio;

    private String phone;

    private String mail;

    private UserDTO user;
}
