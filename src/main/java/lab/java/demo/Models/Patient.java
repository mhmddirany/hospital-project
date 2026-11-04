package lab.java.demo.Models;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "patients")
public class Patient implements Comparable<Patient> {

    /**
     * Database-generated primary key (step 1: replaces the old in-memory
     * AtomicInteger counter). {@code null} until the entity is first saved.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int age;

    // Issue 16 follow-up: optional contact info, needed so a real
    // email/SMS provider (as opposed to the Console demo stand-ins) has
    // somewhere to actually send to. Both are nullable -- a patient
    // registered without them simply can't receive a real reminder yet;
    // SmtpEmailNotifier/TwilioSmsNotifier log a warning and skip rather
    // than fail the whole reminder batch over one missing address.
    @Column
    private String email;

    @Column(length = 32)
    private String phone;

    @Embedded
    private MedicalRecord medicalRecord = new MedicalRecord();

    /** Required by JPA; not for application code. */
    protected Patient() {
    }

    public Patient(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public MedicalRecord getMedicalRecord() {
        // Hibernate loads an embedded object as null when all of its
        // columns are null; never hand callers a null record.
        if (medicalRecord == null) {
            medicalRecord = new MedicalRecord();
        }
        return medicalRecord;
    }

    // Issue 19: register()/login() used to live here as stubs that always
    // returned true without checking or storing anything. searchDoctor()
    // used to always return an empty list; real doctor search lives at
    // DoctorService.findBySpecialty(...) / GET /api/doctors/specialty/{s}.
    // Removed rather than faked.

    // ---------- equals / hashCode ----------
    // Identity is the database id. Two unsaved instances (id still null)
    // are only equal to themselves. hashCode is deliberately constant per
    // class so it doesn't change when the id is assigned on save -- the
    // standard pattern for JPA entities with generated ids.

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Patient)) return false;
        Patient other = (Patient) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public int compareTo(Patient other) {
        int cmp = this.name.compareToIgnoreCase(other.name);
        if (cmp != 0) return cmp;
        return Integer.compare(this.id == null ? 0 : this.id, other.id == null ? 0 : other.id);
    }
}
