package tuchin_emelianov.blps_lab_1.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SetPaymentTypeRequest {

    private String type;

    private Long id;

}
