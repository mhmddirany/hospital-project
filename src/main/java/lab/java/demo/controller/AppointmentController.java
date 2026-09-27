package lab.java.demo.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lab.java.demo.Models.Appointment;
import lab.java.demo.Models.NotificationChannel;
import lab.java.demo.dto.ApiResponse;
import lab.java.demo.dto.AppointmentRequest;
import lab.java.demo.dto.AppointmentResponse;
import lab.java.demo.service.AppointmentService;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ApiResponse<AppointmentResponse> requestAppointment(@Valid @RequestBody AppointmentRequest request) {
        Appointment appt = appointmentService.createAppointment(
                request.getPatientId(), request.getDoctorId(), request.getDateTime());
        return new ApiResponse<>("Appointment requested", AppointmentResponse.from(appt));
    }

    @PutMapping("/{appointmentId}/confirm")
    public ApiResponse<AppointmentResponse> confirm(@PathVariable int appointmentId) {
        Appointment appt = appointmentService.confirmAppointment(appointmentId);
        return new ApiResponse<>("Appointment confirmed", AppointmentResponse.from(appt));
    }

    @PutMapping("/{appointmentId}/cancel")
    public ApiResponse<AppointmentResponse> cancel(@PathVariable int appointmentId) {
        Appointment appt = appointmentService.cancelAppointment(appointmentId);
        return new ApiResponse<>("Appointment cancelled", AppointmentResponse.from(appt));
    }

    @GetMapping("/patient/{patientId}")
    public ApiResponse<List<AppointmentResponse>> listAppointmentsForPatient(@PathVariable int patientId) {
        List<AppointmentResponse> result = appointmentService.getAppointmentsForPatient(patientId).stream()
                .map(AppointmentResponse::from)
                .toList();
        return new ApiResponse<>("Appointments for patient id " + patientId, result);
    }

    @GetMapping("/doctor/{doctorId}")
    public ApiResponse<List<AppointmentResponse>> listAppointmentsForDoctorSorted(@PathVariable int doctorId) {
        List<AppointmentResponse> result = appointmentService.getAppointmentsForDoctorSortedByDate(doctorId).stream()
                .map(AppointmentResponse::from)
                .toList();
        return new ApiResponse<>("Appointments for doctor id " + doctorId + " sorted by date", result);
    }

    @PostMapping("/reminders/tomorrow")
    public ApiResponse<Void> sendRemindersForTomorrow(@RequestBody List<NotificationChannel> channels) {
        appointmentService.sendRemindersForTomorrow(channels);
        return new ApiResponse<>("Reminders sent for tomorrow's appointments", null);
    }
}
