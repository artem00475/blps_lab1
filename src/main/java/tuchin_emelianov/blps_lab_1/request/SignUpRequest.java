package tuchin_emelianov.blps_lab_1.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignUpRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @NotBlank
    private String fio;
    @NotBlank
    private String phone;
    @NotBlank
    private String mail;
    @NotBlank
    private String role;
}
