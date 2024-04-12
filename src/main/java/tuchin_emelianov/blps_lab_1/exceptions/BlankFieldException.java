package tuchin_emelianov.blps_lab_1.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BlankFieldException extends RuntimeException{
    public BlankFieldException(String message) {
        super(message);
    }
}
