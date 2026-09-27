package lab.java.demo.dto;

import lab.java.demo.Models.Doctor;

/**
 * Public shape of a Doctor: only the fields clients need, decoupled from the
 * internal domain class so it can change without breaking the API contract.
 */
public class DoctorResponse {

    private final int id;
    private final String name;
    private final int age;
    private final String specialty;
    private final boolean availability;

    public DoctorResponse(int id, String name, int age, String specialty, boolean availability) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.specialty = specialty;
        this.availability = availability;
    }

    public static DoctorResponse from(Doctor doctor) {
        return new DoctorResponse(
                doctor.getId(), doctor.getName(), doctor.getAge(),
                doctor.getSpecialty(), doctor.isAvailability());
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getSpecialty() { return specialty; }
    public boolean isAvailability() { return availability; }
}
