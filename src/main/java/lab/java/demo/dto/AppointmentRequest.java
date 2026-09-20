package lab.java.demo.dto;

import lab.java.demo.Models.Doctor;
import lab.java.demo.Models.Patient;

import java.time.LocalDateTime;

public class AppointmentRequest {
    private Patient patient;
    private Doctor doctor;
    private LocalDateTime dateTime;

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
}
