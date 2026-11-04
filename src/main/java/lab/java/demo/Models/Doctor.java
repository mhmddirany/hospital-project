package lab.java.demo.Models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "doctors")
public class Doctor extends Employee implements Comparable<Doctor> {

    /**
     * Database-generated primary key (step 1: replaces the old in-memory
     * AtomicInteger counter). {@code null} until the entity is first saved.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String specialty;

    @Column(nullable = false)
    private boolean availability = true;

    // Step 1: primaryCarePatients/viewPatients()/addPatientToPanel() and
    // materialsUsed() were removed -- nothing in the application ever
    // called them, and mapping an unused doctor<->patient relationship
    // into the database would only add a table nobody reads. Add a real
    // relationship back when a feature actually needs it.

    /** Required by JPA; not for application code. */
    protected Doctor() {
    }

    public Doctor(String name, int age, String specialty) {
        super(name, age);
        this.specialty = specialty;
    }

    public Integer getId() { return id; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
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
        if (!(o instanceof Doctor)) return false;
        Doctor other = (Doctor) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // Natural ordering: by name (case-insensitive), then id
    @Override
    public int compareTo(Doctor other) {
        int cmp = this.getName().compareToIgnoreCase(other.getName());
        if (cmp != 0) return cmp;
        return Integer.compare(this.id == null ? 0 : this.id, other.id == null ? 0 : other.id);
    }
}
