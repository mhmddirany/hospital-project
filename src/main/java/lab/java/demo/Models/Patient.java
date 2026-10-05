package lab.java.demo.Models;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Patient implements Comparable<Patient> {
    private static final AtomicInteger ID = new AtomicInteger(1);

    private final int id;
    private String name;
    private int age;

    // Issue 16 follow-up: optional contact info, needed so a real
    // email/SMS provider (as opposed to the Console demo stand-ins) has
    // somewhere to actually send to. Both are nullable -- a patient
    // registered without them simply can't receive a real reminder yet;
    // SmtpEmailNotifier/TwilioSmsNotifier log a warning and skip rather
    // than fail the whole reminder batch over one missing address.
    private String email;
    private String phone;

    private final MedicalRecord medicalRecord = new MedicalRecord();

    public Patient(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public static int nextId() { return ID.getAndIncrement(); }

    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public MedicalRecord getMedicalRecord() { return medicalRecord; }

    // Issue 19: register()/login() used to live here as stubs that always
    // returned true without checking or storing anything -- there is no
    // authentication system yet (that's Issue 21). searchDoctor() used to
    // always return an empty list; real doctor search already exists at
    // DoctorService.findBySpecialty(...) / GET /api/doctors/specialty/{s},
    // so the empty-list stub here was redundant as well as dishonest.
    // Removed rather than faked.

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

    @Override
    public int compareTo(Patient other) {
        int cmp = this.name.compareToIgnoreCase(other.name);
        if (cmp != 0) return cmp;
        return Integer.compare(this.id, other.id);
    }
}
