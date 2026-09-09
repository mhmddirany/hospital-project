package lab.java.demo.Models;

import java.time.LocalDateTime;
import java.util.Objects;

public class Receptionist extends Employee implements Comparable<Receptionist> {
    private final int id;
    private boolean availability = true;

    public Receptionist(int id, String name, int age) {
        super(name, age);
        this.id = id;
    }

    public int getId() { return id; }
    public boolean isAvailability() { return availability; }
    public void setAvailability(boolean availability) { this.availability = availability; }

    public Appointment scheduleAppointment(int patientId, int doctorId, LocalDateTime dateTime,
                                           Patient patient, Doctor doctor) {
        Appointment appt = new Appointment(Appointment.nextId(), dateTime, "scheduled", patient, doctor);
        patient.addAppointment(appt);
        return appt;
    }

    public boolean cancelAppointment(int apptId) { return true; }
    public boolean checkAvailability(int doctorId) { return true; }

    // ---------- equals / hashCode / Comparable ----------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Receptionist)) return false;
        Receptionist that = (Receptionist) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Natural ordering: by name, then id
    @Override
    public int compareTo(Receptionist other) {
        int cmp = this.getName().compareToIgnoreCase(other.getName());
        if (cmp != 0) return cmp;
        return Integer.compare(this.id, other.id);
    }
}
