package lab.java.demo.controller;

import lab.java.demo.Models.Patient;
import lab.java.demo.dto.ApiResponse;
import lab.java.demo.service.PatientService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    public ApiResponse<Patient> register(@RequestBody Patient p) {
        Patient saved = patientService.registerPatient(p);
        return new ApiResponse<>("Patient registered successfully", saved);
    }

    @GetMapping("/{id}")
    public ApiResponse<Optional<Patient>> getById(@PathVariable int id) {
        Optional<Patient> p = patientService.findById(id);
        String msg = p.isPresent() ? "Patient found" : "Patient not found";
        return new ApiResponse<>(msg, p);
    }

    @GetMapping("/search/{name}")
    public ApiResponse<List<Patient>> searchByName(@PathVariable String name) {
        List<Patient> result = patientService.searchByName(name);
        return new ApiResponse<>("Patients matching: " + name, result);
    }

    @GetMapping("/sorted/name")
    public ApiResponse<List<Patient>> listAllSortedByName() {
        return new ApiResponse<>("All patients sorted by name",
                patientService.getAllSortedByName());
    }

    @GetMapping("/sorted/age")
    public ApiResponse<List<Patient>> listAllSortedByAgeDesc() {
        return new ApiResponse<>("All patients sorted by age (desc)",
                patientService.getAllSortedByAgeDescending());
    }
}
