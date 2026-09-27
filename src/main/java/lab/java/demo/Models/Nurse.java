package lab.java.demo.Models;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Nurse extends Employee implements Comparable<Nurse> {
    private static final AtomicInteger ID = new AtomicInteger(1);

    private final int id;
    private String department;
    private boolean availability = true;

    public Nurse(int id, String name, int age, String department) {
        super(name, age);
        this.id = id;
        this.department = department;
    }

    public static int nextId() { return ID.getAndIncrement(); }

    public int getId() { return id; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public boolean isAvailability() { return availability; }
    public void setAvailability(boolean availability) { this.availability = availability; }

    // Issue 19: recordVitals/triageAssessment/addNursingNote/checkIn/
    // checkOut/acceptTask used to live here as stubs that always returned
    // true (or did nothing) without recording anything -- there was never
    // a vitals/triage/notes/task repository or service backing them.
    // Removed rather than faked; add them back for real once that data
    // actually has somewhere to live.

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
