package tuchin_emelianov.blps_lab_1.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {
    private String content;

    private String object;

    private boolean isRoles;

    private List<String> to;
}
