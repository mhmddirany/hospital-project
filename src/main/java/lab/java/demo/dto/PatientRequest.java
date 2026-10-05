package lab.java.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class PatientRequest {

    @NotBlank(message = "name must not be blank")
    private String name;

    @Min(value = 0, message = "age must not be negative")
    @Max(value = 120, message = "age must be 120 or below")
    private int age;

    // Issue 16 follow-up: optional -- a real email/SMS reminder needs
    // somewhere to send to, but registration shouldn't require contact
    // info that not every patient has on hand yet. @Email/@Pattern only
    // validate format when a value is actually supplied; null/blank is
    // left alone (see jakarta.validation semantics for optional fields).
    @Email(message = "email must be a well-formed address")
    private String email;

    // Matches empty string too (not just null) -- @Pattern, unlike
    // @Email, treats "" as a value to validate against the regex rather
    // than as "not supplied", so the alternation keeps an omitted/blank
    // phone valid without requiring callers to send null specifically.
    @Pattern(regexp = "^(|[0-9+()\\-\\s]{7,20})$", message = "phone must be 7-20 digits, optionally with +()- or spaces")
    private String phone;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
