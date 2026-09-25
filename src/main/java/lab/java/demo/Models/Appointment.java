package lab.java.demo.Models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import lab.java.demo.exception.InvalidAppointmentException;

public class Appointment implements Comparable<Appointment> {
    private static final AtomicInteger ID = new AtomicInteger(1);

    private final int id;
    private LocalDateTime dateTime;
    private AppointmentStatus status;

    private final Patient patient;
    private final Doctor doctor;

    public Appointment(int id, LocalDateTime dateTime, AppointmentStatus status, Patient patient, Doctor doctor) {
        this.id = id;
        this.dateTime = dateTime;
        this.status = status;
        this.patient = patient;
        this.doctor = doctor;
    }

    public static int nextId() { return ID.getAndIncrement(); }

    public int getId() { return id; }
    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
    public AppointmentStatus getStatus() { return status; }
    public Patient getPatient() { return patient; }
    public Doctor getDoctor() { return doctor; }

    public boolean confirm() {
        if (status == AppointmentStatus.CANCELED) {
            throw new InvalidAppointmentException(
                    "Cannot confirm appointment #" + id + " because it has already been canceled");
        }
        if (status == AppointmentStatus.CONFIRMED) {
            return false;
        }
        this.status = AppointmentStatus.CONFIRMED;
        return true;
    }

    public boolean cancel() {
        if (status == AppointmentStatus.CANCELED) {
            return false;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Appointment)) return false;
        Appointment that = (Appointment) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Appointment other) {
        int cmp = this.dateTime.compareTo(other.dateTime);
        if (cmp != 0) return cmp;
        return Integer.compare(this.id, other.id);
    }
}
