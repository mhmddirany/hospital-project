package lab.java.demo.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lab.java.demo.Models.Doctor;
import lab.java.demo.dto.ApiResponse;
import lab.java.demo.service.DoctorService;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping
    public ApiResponse<Doctor> addDoctor(@RequestBody Doctor d) {
        Doctor saved = doctorService.addDoctor(d);
        return new ApiResponse<>("Doctor added successfully", saved);
    }

    @GetMapping("/{id}")
    public ApiResponse<Optional<Doctor>> getById(@PathVariable int id) {
        Optional<Doctor> d = doctorService.findById(id);
        String msg = d.isPresent() ? "Doctor found" : "Doctor not found";
        return new ApiResponse<>(msg, d);
    }

    @GetMapping("/specialty/{specialty}")
    public ApiResponse<List<Doctor>> findBySpecialty(@PathVariable String specialty) {
        List<Doctor> result = doctorService.findBySpecialty(specialty);
        return new ApiResponse<>("Doctors with specialty: " + specialty, result);
    }

    @GetMapping("/sorted/name")
    public ApiResponse<List<Doctor>> listAllSortedByName() {
        return new ApiResponse<>("All doctors sorted by name",
                doctorService.getAllSortedByName());
    }

    @GetMapping("/sorted/specialty")
    public ApiResponse<List<Doctor>> listAllSortedBySpecialty() {
        return new ApiResponse<>("All doctors sorted by specialty then name",
                doctorService.getAllSortedBySpecialty());
    }
}