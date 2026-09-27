package lab.java.demo.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lab.java.demo.Models.Doctor;
import lab.java.demo.dto.ApiResponse;
import lab.java.demo.dto.DoctorRequest;
import lab.java.demo.dto.DoctorResponse;
import lab.java.demo.service.DoctorService;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping
    public ApiResponse<DoctorResponse> addDoctor(@Valid @RequestBody DoctorRequest request) {
        Doctor doctor = new Doctor(Doctor.nextId(), request.getName(), request.getAge(), request.getSpecialty());
        Doctor saved = doctorService.addDoctor(doctor);
        return new ApiResponse<>("Doctor added successfully", DoctorResponse.from(saved));
    }

    @GetMapping("/{id}")
    public ApiResponse<DoctorResponse> getById(@PathVariable int id) {
        Doctor doctor = doctorService.getByIdOrThrow(id);
        return new ApiResponse<>("Doctor found", DoctorResponse.from(doctor));
    }

    @GetMapping("/specialty/{specialty}")
    public ApiResponse<List<DoctorResponse>> findBySpecialty(@PathVariable String specialty) {
        List<DoctorResponse> result = doctorService.findBySpecialty(specialty).stream()
                .map(DoctorResponse::from)
                .toList();
        return new ApiResponse<>("Doctors with specialty: " + specialty, result);
    }

    @GetMapping("/sorted/name")
    public ApiResponse<List<DoctorResponse>> listAllSortedByName() {
        List<DoctorResponse> result = doctorService.getAllSortedByName().stream()
                .map(DoctorResponse::from)
                .toList();
        return new ApiResponse<>("All doctors sorted by name", result);
    }

    @GetMapping("/sorted/specialty")
    public ApiResponse<List<DoctorResponse>> listAllSortedBySpecialty() {
        List<DoctorResponse> result = doctorService.getAllSortedBySpecialty().stream()
                .map(DoctorResponse::from)
                .toList();
        return new ApiResponse<>("All doctors sorted by specialty then name", result);
    }

    @GetMapping("/{id}/availability")
    public ApiResponse<Boolean> checkAvailability(@PathVariable int id) {
        boolean available = doctorService.isAvailable(id);
        return new ApiResponse<>("Availability for doctor id " + id, available);
    }
}
