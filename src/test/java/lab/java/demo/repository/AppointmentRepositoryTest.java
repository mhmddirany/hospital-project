package lab.java.demo.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import lab.java.demo.Models.*;

/**
 * Step 1: a {@code @DataJpaTest} starts only the JPA layer (entities,
 * repositories, Flyway) against the test database -- no web layer, no
 * security. Each test runs in a transaction that is rolled back at the
 * end, so tests never see each other's data.
 *
 * <p>{@code replace = NONE} keeps the H2-in-PostgreSQL-mode database from
 * application-test.properties instead of Spring's default embedded one.
 * {@code flushAndClear()} forces a real INSERT and makes the next read go
 * back to the database, so these tests check what was actually stored,
 * not just objects sitting in Hibernate's cache.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AppointmentRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private AppointmentRepository repository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    private static final LocalDateTime SLOT = LocalDateTime.now().plusDays(3).withHour(9).withMinute(0).withSecond(0).withNano(0);

    private Appointment book(Patient p, Doctor d, LocalDateTime at, AppointmentStatus status) {
        return repository.save(new Appointment(at, status, p, d));
    }

    private Patient patient(String name) {
        return patientRepository.save(new Patient(name, 40));
    }

    private Doctor doctor(String name) {
        return doctorRepository.save(new Doctor(name, 50, "Cardiology"));
    }

    @Test
    void saveStoresTheForeignKeysAndStatus() {
        Patient p = patient("Alex Rivera");
        Doctor d = doctor("Dr. Kim");
        Integer id = book(p, d, SLOT, AppointmentStatus.REQUESTED).getId();
        flushAndClear();

        Appointment found = repository.findById(id).orElseThrow();
        assertEquals(p.getId(), found.getPatient().getId());
        assertEquals("Dr. Kim", found.getDoctor().getName());
        assertEquals(AppointmentStatus.REQUESTED, found.getStatus());
        assertEquals(SLOT, found.getDateTime());
    }

    @Test
    void aStatusChangeIsStoredOnSave() {
        Appointment appt = book(patient("Alex Rivera"), doctor("Dr. Kim"), SLOT, AppointmentStatus.REQUESTED);
        appt.confirm();
        repository.save(appt);
        flushAndClear();
        assertEquals(AppointmentStatus.CONFIRMED, repository.findById(appt.getId()).orElseThrow().getStatus());
    }

    @Test
    void findByPatientIdAndFindByDoctorIdFilterCorrectly() {
        Patient alex = patient("Alex Rivera");
        Patient blair = patient("Blair Chen");
        Doctor kim = doctor("Dr. Kim");
        Doctor patel = doctor("Dr. Patel");
        book(alex, kim, SLOT, AppointmentStatus.REQUESTED);
        book(blair, kim, SLOT.plusHours(1), AppointmentStatus.REQUESTED);
        book(alex, patel, SLOT.plusHours(2), AppointmentStatus.REQUESTED);
        flushAndClear();

        assertEquals(2, repository.findByPatientId(alex.getId()).size());
        assertEquals(2, repository.findByDoctorId(kim.getId()).size());
        assertEquals(1, repository.findByDoctorId(patel.getId()).size());
    }

    @Test
    void doubleBookingQueryIgnoresCancelledAppointments() {
        Patient p = patient("Alex Rivera");
        Doctor d = doctor("Dr. Kim");
        book(p, d, SLOT, AppointmentStatus.CANCELED);
        flushAndClear();
        assertFalse(repository.existsByDoctorIdAndDateTimeAndStatusNot(d.getId(), SLOT, AppointmentStatus.CANCELED));

        book(p, d, SLOT, AppointmentStatus.REQUESTED);
        flushAndClear();
        assertTrue(repository.existsByDoctorIdAndDateTimeAndStatusNot(d.getId(), SLOT, AppointmentStatus.CANCELED));
        assertFalse(repository.existsByDoctorIdAndDateTimeAndStatusNot(d.getId(), SLOT.plusMinutes(30), AppointmentStatus.CANCELED));
    }

    @Test
    void reminderWindowQueryIsHalfOpenAndFiltersByStatus() {
        Patient p = patient("Alex Rivera");
        Doctor d = doctor("Dr. Kim");
        LocalDateTime start = SLOT.toLocalDate().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        Appointment atStart = book(p, d, start, AppointmentStatus.CONFIRMED);
        book(p, d, end, AppointmentStatus.CONFIRMED);                 // excluded: end is exclusive
        book(p, d, start.plusHours(10), AppointmentStatus.REQUESTED); // excluded: not confirmed
        flushAndClear();

        List<Appointment> due = repository.findByStatusAndDateTimeGreaterThanEqualAndDateTimeLessThan(
                AppointmentStatus.CONFIRMED, start, end);
        assertEquals(1, due.size());
        assertEquals(atStart.getId(), due.get(0).getId());
    }

    @Test
    void theForeignKeyRejectsAnAppointmentForAPatientThatDoesNotExist() {
        // The database's foreign key constraint, not application code,
        // now guarantees an appointment always points at a real patient.
        Doctor d = doctor("Dr. Kim");
        Patient missing = em.getEntityManager().getReference(Patient.class, 404_404);
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new Appointment(SLOT, AppointmentStatus.REQUESTED, missing, d)));
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}
