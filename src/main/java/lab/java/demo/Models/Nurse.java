package lab.java.demo.Models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "nurses")
public class Nurse extends Employee implements Comparable<Nurse> {

    /**
     * Database-generated primary key (step 1: replaces the old in-memory
     * AtomicInteger counter). {@code null} until the entity is first saved.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private boolean availability = true;

    // Issue 19: recordVitals/triageAssessment/addNursingNote/checkIn/
    // checkOut/acceptTask used to live here as stubs that always returned
    // true (or did nothing) without recording anything -- there was never
    // a vitals/triage/notes/task repository or service backing them.
    // Removed rather than faked; add them back for real once that data
    // actually has somewhere to live.

    /** Required by JPA; not for application code. */
    protected Nurse() {
    }

    public Nurse(String name, int age, String department) {
        super(name, age);
        this.department = department;
    }

    public Integer getId() { return id; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public boolean isAvailability() { return availability; }
    public void setAvailability(boolean availability) { this.availability = availability; }

    // ---------- equals / hashCode ----------
    // Identity is the database id. Two unsaved instances (id still null)
    // are only equal to themselves. hashCode is deliberately constant per
    // class so it doesn't change when the id is assigned on save -- the
    // standard pattern for JPA entities with generated ids.

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Nurse)) return false;
        Nurse other = (Nurse) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // Natural ordering: by name (case-insensitive), then id
    @Override
    public int compareTo(Nurse other) {
        int cmp = this.getName().compareToIgnoreCase(other.getName());
        if (cmp != 0) return cmp;
        return Integer.compare(this.id == null ? 0 : this.id, other.id == null ? 0 : other.id);
    }
}
