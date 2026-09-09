package lab.java.demo.controller;

import lab.java.demo.Models.Doctor;
import lab.java.demo.dto.ApiResponse;
import lab.java.demo.service.DoctorService;

import java.util.List;
import java.util.Optional;

public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    public ApiResponse<Doctor> addDoctor(Doctor d) {
        Doctor saved = doctorService.addDoctor(d);
        return new ApiResponse<>("Doctor added successfully", saved);
    }

    public ApiResponse<Optional<Doctor>> getById(int id) {
        Optional<Doctor> d = doctorService.findById(id);
        String msg = d.isPresent() ? "Doctor found" : "Doctor not found";
        return new ApiResponse<>(msg, d);
    }

    public ApiResponse<List<Doctor>> findBySpecialty(String specialty) {
        List<Doctor> result = doctorService.findBySpecialty(specialty);
        return new ApiResponse<>("Doctors with specialty: " + specialty, result);
    }

    public ApiResponse<List<Doctor>> listAllSortedByName() {
        return new ApiResponse<>("All doctors sorted by name",
                doctorService.getAllSortedByName());
    }

    public ApiResponse<List<Doctor>> listAllSortedBySpecialty() {
        return new ApiResponse<>("All doctors sorted by specialty then name",
                doctorService.getAllSortedBySpecialty());
    }
}
