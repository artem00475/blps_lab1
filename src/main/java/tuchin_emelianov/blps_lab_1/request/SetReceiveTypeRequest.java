package tuchin_emelianov.blps_lab_1.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SetReceiveTypeRequest {

    private String type;

    private Long id;

    private String address;

}
