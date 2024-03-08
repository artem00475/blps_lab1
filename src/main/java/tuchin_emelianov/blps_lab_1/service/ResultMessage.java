package tuchin_emelianov.blps_lab_1.service;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResultMessage {
    private long id;

    private String message;

    public ResultMessage(long id) {
        this.id = id;
    }

    public ResultMessage(long id, String message) {
        this.id = id;
        this.message = message;
    }
}
