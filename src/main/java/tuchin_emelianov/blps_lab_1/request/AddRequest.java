package tuchin_emelianov.blps_lab_1.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AddRequest {

    private Long userId;

    private String fio;

    private String phone;

    private String mail;

    private List<Product> products;
}
