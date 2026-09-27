package lab.java.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class DoctorRequest {

    @NotBlank(message = "name must not be blank")
    private String name;

    @Min(value = 18, message = "age must be at least 18")
    @Max(value = 100, message = "age must be 100 or below")
    private int age;

    @NotBlank(message = "specialty must not be blank")
    private String specialty;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
}
