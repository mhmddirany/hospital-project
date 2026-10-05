package lab.java.demo.dto;

import lab.java.demo.Models.Patient;

/**
 * Public shape of a Patient. Deliberately excludes MedicalRecord: no current
 * endpoint reads or writes it, and returning it by default would leak
 * conditions/medications to any caller who can look up a patient.
 */
public class PatientResponse {

    private final int id;
    private final String name;
    private final int age;
    private final String email;
    private final String phone;

    public PatientResponse(int id, String name, int age, String email, String phone) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.email = email;
        this.phone = phone;
    }

    public static PatientResponse from(Patient patient) {
        return new PatientResponse(patient.getId(), patient.getName(), patient.getAge(),
                patient.getEmail(), patient.getPhone());
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
}
