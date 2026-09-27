package lab.java.demo.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public class AppointmentRequest {

    @Positive(message = "patientId must be a positive number")
    private int patientId;

    @Positive(message = "doctorId must be a positive number")
    private int doctorId;

    @NotNull(message = "dateTime must not be null")
    @Future(message = "dateTime must be in the future")
    private LocalDateTime dateTime;

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
}
