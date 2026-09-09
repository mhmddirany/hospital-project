package lab.java.demo.controller;

import lab.java.demo.Models.Appointment;
import lab.java.demo.Models.Doctor;
import lab.java.demo.Models.Patient;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentControllerTest {

    private final List<Appointment> appointments = new ArrayList<>();

    @PostMapping("/create")
    public Appointment create(@RequestBody Map<String, String> body) {

        Patient patient = new Patient(
                Integer.parseInt(body.get("patientId")),
                body.get("patientName"),
                Integer.parseInt(body.get("patientAge"))
        );

        Doctor doctor = new Doctor(
                Integer.parseInt(body.get("doctorId")),
                body.get("doctorName"),
                40,
                body.get("specialty")
        );

        LocalDateTime dt = LocalDateTime.parse(body.get("dateTime"));

        Appointment appt = new Appointment(
                Appointment.nextId(),
                dt,
                "scheduled",
                patient,
                doctor
        );

        appointments.add(appt);
        return appt;
    }

    @GetMapping
    public List<Appointment> getAll() {
        return appointments;
    }
}
