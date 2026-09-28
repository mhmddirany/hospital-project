package lab.java.demo.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lab.java.demo.Models.Appointment;
import lab.java.demo.Models.AppointmentStatus;
import lab.java.demo.Models.Doctor;
import lab.java.demo.Models.Patient;

class AppointmentRepositoryTest {

    private AppointmentRepository repository;
    private Patient patient;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        repository = new AppointmentRepository();
        patient = new Patient(1, "Alex Rivera", 40);
        doctor = new Doctor(1, "Dr. Kim", 50, "Cardiology");
    }

    private Appointment appointment(int id, LocalDateTime dateTime, AppointmentStatus status) {
        return new Appointment(id, dateTime, status, patient, doctor);
    }

    @Test
    void savesAndFindsById() {
        Appointment saved = repository.save(appointment(1, LocalDateTime.now().plusDays(1), AppointmentStatus.REQUESTED));

        Optional<Appointment> found = repository.findById(1);
        assertTrue(found.isPresent());
        assertEquals(saved, found.get());
    }

    @Test
    void findByIdReturnsEmptyWhenMissing() {
        assertTrue(repository.findById(404).isEmpty());
    }

    @Test
    void saveOverwritesAnExistingIdInsteadOfRejectingIt() {
        // Unlike the person/staff repositories, appointment ids are always
        // server-generated (never client-supplied), so there's no separate
        // "reject duplicates" contract to enforce here -- save() is also
        // how confirmAppointment()/cancelAppointment() persist an updated
        // status for an id that already exists.
        repository.save(appointment(1, LocalDateTime.now().plusDays(1), AppointmentStatus.REQUESTED));
        repository.save(appointment(1, LocalDateTime.now().plusDays(1), AppointmentStatus.CONFIRMED));

        assertEquals(AppointmentStatus.CONFIRMED, repository.findById(1).orElseThrow().getStatus());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void findByPatientIdAndFindByDoctorIdFilterCorrectly() {
        Patient otherPatient = new Patient(2, "Blair Chen", 30);
        Doctor otherDoctor = new Doctor(2, "Dr. Patel", 45, "Neurology");

        repository.save(appointment(1, LocalDateTime.now().plusDays(1), AppointmentStatus.REQUESTED));
        repository.save(new Appointment(2, LocalDateTime.now().plusDays(2), AppointmentStatus.REQUESTED, otherPatient, doctor));
        repository.save(new Appointment(3, LocalDateTime.now().plusDays(3), AppointmentStatus.REQUESTED, patient, otherDoctor));

        List<Appointment> forPatient = repository.findByPatientId(1);
        assertEquals(2, forPatient.size());

        List<Appointment> forDoctor = repository.findByDoctorId(1);
        assertEquals(2, forDoctor.size());
    }

    @Test
    void findByDateRangeIsInclusiveOnBothEnds() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(2);

        Appointment atStart = repository.save(appointment(1, start, AppointmentStatus.REQUESTED));
        Appointment atEnd = repository.save(appointment(2, end, AppointmentStatus.REQUESTED));
        repository.save(appointment(3, end.plusSeconds(1), AppointmentStatus.REQUESTED));

        List<Appointment> inRange = repository.findByDateRange(start, end);
        assertEquals(2, inRange.size());
        assertTrue(inRange.contains(atStart));
        assertTrue(inRange.contains(atEnd));
    }

    @Test
    void deleteByIdRemovesTheRecordAndReportsWhetherAnythingWasRemoved() {
        repository.save(appointment(1, LocalDateTime.now().plusDays(1), AppointmentStatus.REQUESTED));

        assertTrue(repository.deleteById(1));
        assertFalse(repository.deleteById(1));
    }
}
