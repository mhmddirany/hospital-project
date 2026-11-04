package lab.java.demo.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import lab.java.demo.Models.Appointment;
import lab.java.demo.Models.AppointmentStatus;

/**
 * Step 1: a Spring Data JPA repository backed by the database, replacing
 * the old ConcurrentHashMap. JpaRepository already provides save, findById,
 * findAll, existsById, deleteById, count and paging; Spring writes the SQL
 * for each extra method below from its name (a "derived query").
 *
 * <p>The filtering that used to happen in Java (streaming over every
 * appointment) now happens in the database, using the indexes created in
 * V1__initial_schema.sql.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    /** "PatientId" walks the relationship: WHERE patient_id = :patientId */
    List<Appointment> findByPatientId(Integer patientId);

    List<Appointment> findByDoctorId(Integer doctorId);

    /** Double-booking check (Issue 3): does this doctor already have a non-cancelled appointment at exactly this time? */
    boolean existsByDoctorIdAndDateTimeAndStatusNot(Integer doctorId, LocalDateTime dateTime,
                                                    AppointmentStatus excludedStatus);

    /** Reminder window (Issues 13/14): half-open [start, end), one status. */
    List<Appointment> findByStatusAndDateTimeGreaterThanEqualAndDateTimeLessThan(
            AppointmentStatus status, LocalDateTime start, LocalDateTime end);
}
