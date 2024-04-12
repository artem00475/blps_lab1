package tuchin_emelianov.blps_lab_1.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UserRequest {
    @NotNull(message = "Id должно быть заполнено")
    @Positive(message = "Id должно быть больше 0")
    private Long id;
}
