package lab.java.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // 409
public class PatientConflictException extends RuntimeException {

    public PatientConflictException(int id) {
        super("Patient with id " + id + " already exists");
    }
}
