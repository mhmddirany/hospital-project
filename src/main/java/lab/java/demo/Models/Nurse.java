package lab.java.demo.Models;

import java.util.Objects;

public class Nurse extends Employee implements Comparable<Nurse> {
    private final int id;
    private String department;
    private boolean availability = true;

    public Nurse(int id, String name, int age, String department) {
        super(name, age);
        this.id = id;
        this.department = department;
    }

    public int getId() { return id; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public boolean isAvailability() { return availability; }
    public void setAvailability(boolean availability) { this.availability = availability; }

    public void recordVitals(int patientId) {}
    public void triageAssessment(int patientId) {}
    public boolean addNursingNote(int patientId, String note) { return true; }
    public boolean checkIn(int patientId, int apptId) { return true; }
    public boolean checkOut(int patientId, int apptId) { return true; }
    public boolean acceptTask(int taskId) { return true; }

    // ---------- equals / hashCode / Comparable ----------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Nurse)) return false;
        Nurse nurse = (Nurse) o;
        return id == nurse.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Natural ordering: by name, then id
    @Override
    public int compareTo(Nurse other) {
        int cmp = this.getName().compareToIgnoreCase(other.getName());
        if (cmp != 0) return cmp;
        return Integer.compare(this.id, other.id);
    }
}
