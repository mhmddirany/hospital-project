package lab.java.demo.Models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "receptionists")
public class Receptionist extends Employee implements Comparable<Receptionist> {

    /**
     * Database-generated primary key (step 1: replaces the old in-memory
     * AtomicInteger counter). {@code null} until the entity is first saved.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private boolean availability = true;

    // Issue 19: checkAvailability(doctorId) used to live here as a stub
    // that always returned true without looking at the doctor at all.
    // Availability is Doctor's data, not something a Receptionist should
    // compute itself -- real callers now use
    // DoctorService.isAvailable(doctorId) / GET /api/doctors/{id}/availability.

    /** Required by JPA; not for application code. */
    protected Receptionist() {
    }

    public Receptionist(String name, int age) {
        super(name, age);
    }

    public Integer getId() { return id; }
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
        if (!(o instanceof Receptionist)) return false;
        Receptionist other = (Receptionist) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // Natural ordering: by name (case-insensitive), then id
    @Override
    public int compareTo(Receptionist other) {
        int cmp = this.getName().compareToIgnoreCase(other.getName());
        if (cmp != 0) return cmp;
        return Integer.compare(this.id == null ? 0 : this.id, other.id == null ? 0 : other.id);
    }
}
