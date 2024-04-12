package tuchin_emelianov.blps_lab_1.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class SetPaymentTypeRequest {
    @NotNull(message = "Способ оплаты быть заполнен")
    @Pattern(regexp = "^Онлайн|При получении$",message="Некорректный способ оплаты")
    private String type;
    @NotNull(message = "Id должно быть заполнено")
    @Positive(message = "Id должно быть больше 0")
    private Long id;

}
