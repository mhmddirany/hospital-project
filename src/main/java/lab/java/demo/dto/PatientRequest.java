package lab.java.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class PatientRequest {

    @NotBlank(message = "name must not be blank")
    private String name;

    @Min(value = 0, message = "age must not be negative")
    @Max(value = 120, message = "age must be 120 or below")
    private int age;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}
