package lab.java.demo.Models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Appointment implements Comparable<Appointment> {
    private static final AtomicInteger ID = new AtomicInteger(1);

    private final int id;
    private LocalDateTime dateTime;
    private String status;

    private final Patient patient;
    private final Doctor doctor;

    public Appointment(int id, LocalDateTime dateTime, String status, Patient patient, Doctor doctor) {
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
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Patient getPatient() { return patient; }
    public Doctor getDoctor() { return doctor; }

    public boolean confirm() {
        this.status = "confirmed";
        return true;
    }

    public boolean cancel() {
        this.status = "canceled";
        return true;
    }

    public void remind(List<Notifier> notifiers) {
        if (notifiers == null) return;
        for (Notifier n : notifiers) {
            n.send(this);
        }
    }

    // ---------- equals / hashCode / Comparable ----------

    // We use id as the unique identifier
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

    // Natural ordering: by dateTime, then by id
    @Override
    public int compareTo(Appointment other) {
        int cmp = this.dateTime.compareTo(other.dateTime);
        if (cmp != 0) return cmp;
        return Integer.compare(this.id, other.id);
    }
}
