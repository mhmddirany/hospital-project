package lab.java.demo.controller;

import lab.java.demo.Models.Nurse;
import lab.java.demo.dto.ApiResponse;
import lab.java.demo.service.NurseService;

import java.util.List;
import java.util.Optional;

public class NurseController {

    private final NurseService nurseService;

    public NurseController(NurseService nurseService) {
        this.nurseService = nurseService;
    }

    public ApiResponse<Nurse> addNurse(Nurse nurse) {
        Nurse saved = nurseService.addNurse(nurse);
        return new ApiResponse<>("Nurse added successfully", saved);
    }

    public ApiResponse<Optional<Nurse>> getById(int id) {
        Optional<Nurse> n = nurseService.findById(id);
        String msg = n.isPresent() ? "Nurse found" : "Nurse not found";
        return new ApiResponse<>(msg, n);
    }

    public ApiResponse<List<Nurse>> findByDepartment(String department) {
        List<Nurse> result = nurseService.findByDepartment(department);
        return new ApiResponse<>("Nurses in department: " + department, result);
    }

    public ApiResponse<List<Nurse>> listAllSortedByName() {
        return new ApiResponse<>("All nurses sorted by name",
                nurseService.getAllSortedByName());
    }
}
