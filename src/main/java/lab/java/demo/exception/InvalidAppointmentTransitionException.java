package lab.java.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // 409
public class InvalidAppointmentTransitionException extends RuntimeException {

    public InvalidAppointmentTransitionException(int appointmentId, String message) {
        super("Appointment #" + appointmentId + ": " + message);
    }
}
