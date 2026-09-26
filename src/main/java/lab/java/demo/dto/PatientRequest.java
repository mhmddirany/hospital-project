package lab.java.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class PatientRequest {

    @Positive(message = "id must be a positive number")
    private int id;

    @NotBlank(message = "name must not be blank")
    private String name;

    @Min(value = 0, message = "age must not be negative")
    @Max(value = 120, message = "age must be 120 or below")
    private int age;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}
