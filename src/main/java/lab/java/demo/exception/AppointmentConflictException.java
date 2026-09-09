package lab.java.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // 409
public class AppointmentConflictException extends RuntimeException {

    public AppointmentConflictException(int doctorId, String dateTime) {
        super("Doctor with id " + doctorId +
              " already has an appointment at " + dateTime);
    }
}
