package lab.java.demo.repository;

import lab.java.demo.Models.Appointment;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory repository that acts like a collection of Appointment objects.
 */
public class AppointmentRepository {

    // Internal storage using Map and List
    private final Map<Integer, Appointment> appointments = new HashMap<>();

    public List<Appointment> findAll() {
        return new ArrayList<>(appointments.values());
    }

    public Optional<Appointment> findById(int id) {
        return Optional.ofNullable(appointments.get(id));
    }

    public Appointment save(Appointment appointment) {
        appointments.put(appointment.getId(), appointment);
        return appointment;
    }

    public boolean deleteById(int id) {
        return appointments.remove(id) != null;
    }

    public List<Appointment> findByPatientId(int patientId) {
        return appointments.values().stream()
                .filter(a -> a.getPatient().getId() == patientId)
                .collect(Collectors.toList());
    }

    public List<Appointment> findByDoctorId(int doctorId) {
        return appointments.values().stream()
                .filter(a -> a.getDoctor().getId() == doctorId)
                .collect(Collectors.toList());
    }

    public List<Appointment> findByDateRange(LocalDateTime from, LocalDateTime to) {
        return appointments.values().stream()
                .filter(a -> !a.getDateTime().isBefore(from) && !a.getDateTime().isAfter(to))
                .collect(Collectors.toList());
    }
}
