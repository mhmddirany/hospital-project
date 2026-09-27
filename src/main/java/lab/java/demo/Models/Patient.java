package lab.java.demo.Models;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Patient implements Comparable<Patient> {
    private static final AtomicInteger ID = new AtomicInteger(1);

    private final int id;
    private String name;
    private int age;

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
    public MedicalRecord getMedicalRecord() { return medicalRecord; }

    public boolean register() { return true; }
    public boolean login() { return true; }

    public List<Doctor> searchDoctor() { return List.of(); }

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
