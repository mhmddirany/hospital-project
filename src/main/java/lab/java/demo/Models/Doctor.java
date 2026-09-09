package lab.java.demo.Models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Doctor extends Employee implements Comparable<Doctor> {
    private final int id;
    private String specialty;
    private boolean availability = true;

    private final List<Patient> primaryCarePatients = new ArrayList<>();

    public Doctor(int id, String name, int age, String specialty) {
        super(name, age);
        this.id = id;
        this.specialty = specialty;
    }

    public int getId() { return id; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public boolean isAvailability() { return availability; }
    public void setAvailability(boolean availability) { this.availability = availability; }

    public boolean approveAppointment(int apptId) { return true; }
    public boolean rejectAppointment(int apptId) { return true; }

    public List<Patient> viewPatients() { return Collections.unmodifiableList(primaryCarePatients); }
    public void addPatientToPanel(Patient p) { primaryCarePatients.add(p); }

    public List<String> materialsUsed() { return List.of(); }

    // ---------- equals / hashCode / Comparable ----------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Doctor)) return false;
        Doctor doctor = (Doctor) o;
        return id == doctor.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Natural ordering: by name (case-insensitive), then id
    @Override
    public int compareTo(Doctor other) {
        int cmp = this.getName().compareToIgnoreCase(other.getName());
        if (cmp != 0) return cmp;
        return Integer.compare(this.id, other.id);
    }
}
