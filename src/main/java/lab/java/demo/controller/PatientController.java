package lab.java.demo.controller;

import lab.java.demo.Models.Patient;
import lab.java.demo.dto.ApiResponse;
import lab.java.demo.service.PatientService;

import java.util.List;
import java.util.Optional;

public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    public ApiResponse<Patient> register(Patient p) {
        Patient saved = patientService.registerPatient(p);
        return new ApiResponse<>("Patient registered successfully", saved);
    }

    public ApiResponse<Optional<Patient>> getById(int id) {
        Optional<Patient> p = patientService.findById(id);
        String msg = p.isPresent() ? "Patient found" : "Patient not found";
        return new ApiResponse<>(msg, p);
    }

    public ApiResponse<List<Patient>> searchByName(String name) {
        List<Patient> result = patientService.searchByName(name);
        return new ApiResponse<>("Patients matching: " + name, result);
    }

    public ApiResponse<List<Patient>> listAllSortedByName() {
        return new ApiResponse<>("All patients sorted by name",
                patientService.getAllSortedByName());
    }

    public ApiResponse<List<Patient>> listAllSortedByAgeDesc() {
        return new ApiResponse<>("All patients sorted by age (desc)",
                patientService.getAllSortedByAgeDescending());
    }
}
