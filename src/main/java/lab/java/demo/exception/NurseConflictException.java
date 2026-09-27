package lab.java.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // 409
public class NurseConflictException extends RuntimeException {

    public NurseConflictException(int id) {
        super("Nurse with id " + id + " already exists");
    }
}
