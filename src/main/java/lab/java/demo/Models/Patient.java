package lab.java.demo.Models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Patient implements Comparable<Patient> {
    private final int id;
    private String name;
    private int age;

    private final MedicalRecord medicalRecord = new MedicalRecord();

    private final List<Appointment> appointments = new ArrayList<>();

    public Patient(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public MedicalRecord getMedicalRecord() { return medicalRecord; }

    public boolean register() { return true; }
    public boolean login() { return true; }

    public List<Doctor> searchDoctor() { return List.of(); }

    public Appointment bookAppointment(int doctorId, LocalDateTime dateTime, Doctor doctor) {
        Appointment appt = new Appointment(Appointment.nextId(), dateTime, "requested", this, doctor);
        appointments.add(appt);
        return appt;
    }

    public boolean cancelAppointment(int appointmentId) {
        for (Appointment a : appointments) {
            if (a.getId() == appointmentId) {
                a.setStatus("canceled");
                return true;
            }
        }
        return false;
    }

    public List<Appointment> getAppointments() { return Collections.unmodifiableList(appointments); }
    void addAppointment(Appointment appt) { appointments.add(appt); }

    // ---------- equals / hashCode / Comparable ----------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Patient)) return false;
        Patient patient = (Patient) o;
        return id == patient.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Natural ordering: by name, then id
    @Override
    public int compareTo(Patient other) {
        int cmp = this.name.compareToIgnoreCase(other.name);
        if (cmp != 0) return cmp;
        return Integer.compare(this.id, other.id);
    }
}
