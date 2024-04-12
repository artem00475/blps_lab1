package tuchin_emelianov.blps_lab_1.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class SetReceiveTypeRequest {
    @NotNull(message = "Способ должен быть заполнен")
    @Pattern(regexp = "^Доставка|Самовывоз$",message="Некорректный способ получения")
    private String type;
    @NotNull(message = "Id должно быть заполнено")
    @Positive(message = "Id должно быть больше 0")
    private Long id;

    private String address;

}
