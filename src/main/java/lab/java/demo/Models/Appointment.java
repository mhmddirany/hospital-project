package lab.java.demo.Models;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lab.java.demo.exception.InvalidAppointmentTransitionException;

@Entity
@Table(name = "appointments")
public class Appointment implements Comparable<Appointment> {

    /**
     * Database-generated primary key (step 1: replaces the old in-memory
     * AtomicInteger counter). {@code null} until the entity is first saved.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    // Stored as the enum's name ("REQUESTED", ...) in a plain varchar
    // column -- never as its ordinal, which would silently change meaning
    // if the enum constants were ever reordered.
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status;

    // Foreign keys to patients/doctors. EAGER because every response that
    // shows an appointment also shows its patient and doctor.
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    /** Required by JPA; not for application code. */
    protected Appointment() {
    }

    public Appointment(LocalDateTime dateTime, AppointmentStatus status, Patient patient, Doctor doctor) {
        this.dateTime = dateTime;
        this.status = status;
        this.patient = patient;
        this.doctor = doctor;
    }

    public Integer getId() { return id; }
    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
    public AppointmentStatus getStatus() { return status; }
    public Patient getPatient() { return patient; }
    public Doctor getDoctor() { return doctor; }

    public boolean confirm() {
        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new InvalidAppointmentTransitionException(id, "cannot confirm a past appointment");
        }
        if (status != AppointmentStatus.REQUESTED) {
            throw new InvalidAppointmentTransitionException(id,
                    "cannot confirm from status " + status);
        }
        this.status = AppointmentStatus.CONFIRMED;
        return true;
    }

    public boolean cancel() {
        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new InvalidAppointmentTransitionException(id, "cannot cancel a past appointment");
        }
        if (status == AppointmentStatus.CANCELED) {
            throw new InvalidAppointmentTransitionException(id, "already canceled");
        }
        this.status = AppointmentStatus.CANCELED;
        return true;
    }

    public void remind(List<Notifier> notifiers) {
        if (notifiers == null) return;
        for (Notifier n : notifiers) {
            n.send(this);
        }
    }

    // ---------- equals / hashCode ----------
    // Identity is the database id. Two unsaved instances (id still null)
    // are only equal to themselves. hashCode is deliberately constant per
    // class so it doesn't change when the id is assigned on save -- the
    // standard pattern for JPA entities with generated ids.

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Appointment)) return false;
        Appointment other = (Appointment) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public int compareTo(Appointment other) {
        int cmp = this.dateTime.compareTo(other.dateTime);
        if (cmp != 0) return cmp;
        return Integer.compare(this.id == null ? 0 : this.id, other.id == null ? 0 : other.id);
    }
}
