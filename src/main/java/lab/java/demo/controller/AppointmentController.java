package lab.java.demo.controller;

import lab.java.demo.Models.Appointment;
import lab.java.demo.Models.Doctor;
import lab.java.demo.Models.Notifier;
import lab.java.demo.Models.Patient;
import lab.java.demo.dto.ApiResponse;
import lab.java.demo.service.AppointmentService;

import java.time.LocalDateTime;
import java.util.List;

public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    public ApiResponse<Appointment> requestAppointment(Patient patient,
                                                       Doctor doctor,
                                                       LocalDateTime dateTime) {
        Appointment appt = appointmentService.createAppointment(patient, doctor, dateTime);
        return new ApiResponse<>("Appointment requested", appt);
    }

    public ApiResponse<Appointment> confirm(int appointmentId) {
        Appointment appt = appointmentService.confirmAppointment(appointmentId);
        return new ApiResponse<>("Appointment confirmed", appt);
    }

    public ApiResponse<Appointment> cancel(int appointmentId) {
        Appointment appt = appointmentService.cancelAppointment(appointmentId);
        return new ApiResponse<>("Appointment cancelled", appt);
    }

    public ApiResponse<List<Appointment>> listAppointmentsForPatient(int patientId) {
        return new ApiResponse<>(
                "Appointments for patient id " + patientId,
                appointmentService.getAppointmentsForPatient(patientId)
        );
    }

    public ApiResponse<List<Appointment>> listAppointmentsForDoctorSorted(int doctorId) {
        return new ApiResponse<>(
                "Appointments for doctor id " + doctorId + " sorted by date",
                appointmentService.getAppointmentsForDoctorSortedByDate(doctorId)
        );
    }

    public ApiResponse<Void> sendRemindersForTomorrow(List<Notifier> notifiers) {
        appointmentService.sendRemindersForTomorrow(notifiers);
        return new ApiResponse<>("Reminders sent for tomorrow's appointments", null);
    }
}
