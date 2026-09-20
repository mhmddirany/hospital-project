package lab.java.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lab.java.demo.Models.Appointment;
import lab.java.demo.Models.Notifier;
import lab.java.demo.dto.ApiResponse;
import lab.java.demo.dto.AppointmentRequest;
import lab.java.demo.service.AppointmentService;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ApiResponse<Appointment> requestAppointment(@RequestBody AppointmentRequest request) {
        Appointment appt = appointmentService.createAppointment(
                request.getPatient(), request.getDoctor(), request.getDateTime());
        return new ApiResponse<>("Appointment requested", appt);
    }

    @PutMapping("/{appointmentId}/confirm")
    public ApiResponse<Appointment> confirm(@PathVariable int appointmentId) {
        Appointment appt = appointmentService.confirmAppointment(appointmentId);
        return new ApiResponse<>("Appointment confirmed", appt);
    }

    @PutMapping("/{appointmentId}/cancel")
    public ApiResponse<Appointment> cancel(@PathVariable int appointmentId) {
        Appointment appt = appointmentService.cancelAppointment(appointmentId);
        return new ApiResponse<>("Appointment cancelled", appt);
    }

    @GetMapping("/patient/{patientId}")
    public ApiResponse<List<Appointment>> listAppointmentsForPatient(@PathVariable int patientId) {
        return new ApiResponse<>(
                "Appointments for patient id " + patientId,
                appointmentService.getAppointmentsForPatient(patientId)
        );
    }

    @GetMapping("/doctor/{doctorId}")
    public ApiResponse<List<Appointment>> listAppointmentsForDoctorSorted(@PathVariable int doctorId) {
        return new ApiResponse<>(
                "Appointments for doctor id " + doctorId + " sorted by date",
                appointmentService.getAppointmentsForDoctorSortedByDate(doctorId)
        );
    }

    @PostMapping("/reminders/tomorrow")
    public ApiResponse<Void> sendRemindersForTomorrow(@RequestBody List<Notifier> notifiers) {
        appointmentService.sendRemindersForTomorrow(notifiers);
        return new ApiResponse<>("Reminders sent for tomorrow's appointments", null);
    }
}