package lab.java.demo.controller;

import lab.java.demo.Models.Patient;
import lab.java.demo.dto.ApiResponse;
import lab.java.demo.dto.PatientRequest;
import lab.java.demo.dto.PatientResponse;
import lab.java.demo.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    public ApiResponse<PatientResponse> register(@Valid @RequestBody PatientRequest request) {
        Patient patient = new Patient(Patient.nextId(), request.getName(), request.getAge());
        // Issue 16 follow-up: optional contact info, so a real
        // email/SMS reminder provider has somewhere to send to.
        patient.setEmail(request.getEmail());
        patient.setPhone(request.getPhone());
        Patient saved = patientService.registerPatient(patient);
        return new ApiResponse<>("Patient registered successfully", PatientResponse.from(saved));
    }

    @GetMapping("/{id}")
    public ApiResponse<PatientResponse> getById(@PathVariable int id) {
        Patient patient = patientService.getByIdOrThrow(id);
        return new ApiResponse<>("Patient found", PatientResponse.from(patient));
    }

    @GetMapping("/search/{name}")
    public ApiResponse<List<PatientResponse>> searchByName(@PathVariable String name) {
        List<PatientResponse> result = patientService.searchByName(name).stream()
                .map(PatientResponse::from)
                .toList();
        return new ApiResponse<>("Patients matching: " + name, result);
    }

    @GetMapping("/sorted/name")
    public ApiResponse<List<PatientResponse>> listAllSortedByName() {
        List<PatientResponse> result = patientService.getAllSortedByName().stream()
                .map(PatientResponse::from)
                .toList();
        return new ApiResponse<>("All patients sorted by name", result);
    }

    @GetMapping("/sorted/age")
    public ApiResponse<List<PatientResponse>> listAllSortedByAgeDesc() {
        List<PatientResponse> result = patientService.getAllSortedByAgeDescending().stream()
                .map(PatientResponse::from)
                .toList();
        return new ApiResponse<>("All patients sorted by age (desc)", result);
    }
}
