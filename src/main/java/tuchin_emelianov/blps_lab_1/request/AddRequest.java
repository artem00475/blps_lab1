package tuchin_emelianov.blps_lab_1.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AddRequest {
    @NotNull
    @NotEmpty
    private List<Product> products;
}
