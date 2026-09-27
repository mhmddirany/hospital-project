package lab.java.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ReceptionistNotFoundException extends RuntimeException {

    public ReceptionistNotFoundException(int id) {
        super("Receptionist with id " + id + " not found");
    }
}
