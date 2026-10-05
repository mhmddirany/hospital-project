package lab.java.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // 409
public class UserConflictException extends RuntimeException {

    public UserConflictException(String message) {
        super(message);
    }
}
