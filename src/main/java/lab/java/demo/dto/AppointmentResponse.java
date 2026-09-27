package lab.java.demo.dto;

import java.time.LocalDateTime;

import lab.java.demo.Models.Appointment;
import lab.java.demo.Models.AppointmentStatus;

/**
 * Public shape of an Appointment. Carries only the patient/doctor fields a
 * client needs to identify who the appointment is for, instead of the full
 * (mutable, medical-record-carrying) Patient and Doctor objects.
 */
public class AppointmentResponse {

    private final int id;
    private final LocalDateTime dateTime;
    private final AppointmentStatus status;
    private final int patientId;
    private final String patientName;
    private final int doctorId;
    private final String doctorName;
    private final String doctorSpecialty;

    public AppointmentResponse(int id, LocalDateTime dateTime, AppointmentStatus status,
                                int patientId, String patientName,
                                int doctorId, String doctorName, String doctorSpecialty) {
        this.id = id;
        this.dateTime = dateTime;
        this.status = status;
        this.patientId = patientId;
        this.patientName = patientName;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.doctorSpecialty = doctorSpecialty;
    }

    public static AppointmentResponse from(Appointment appt) {
        return new AppointmentResponse(
                appt.getId(), appt.getDateTime(), appt.getStatus(),
                appt.getPatient().getId(), appt.getPatient().getName(),
                appt.getDoctor().getId(), appt.getDoctor().getName(), appt.getDoctor().getSpecialty());
    }

    public int getId() { return id; }
    public LocalDateTime getDateTime() { return dateTime; }
    public AppointmentStatus getStatus() { return status; }
    public int getPatientId() { return patientId; }
    public String getPatientName() { return patientName; }
    public int getDoctorId() { return doctorId; }
    public String getDoctorName() { return doctorName; }
    public String getDoctorSpecialty() { return doctorSpecialty; }
}
