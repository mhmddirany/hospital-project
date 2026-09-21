package lab.java.demo.controller;

import lab.java.demo.Models.Nurse;
import lab.java.demo.dto.ApiResponse;
import lab.java.demo.service.NurseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/nurses")
public class NurseController {

    private final NurseService nurseService;

    public NurseController(NurseService nurseService) {
        this.nurseService = nurseService;
    }

    @PostMapping
    public ApiResponse<Nurse> addNurse(@RequestBody Nurse nurse) {
        Nurse saved = nurseService.addNurse(nurse);
        return new ApiResponse<>("Nurse added successfully", saved);
    }

    @GetMapping("/{id}")
    public ApiResponse<Optional<Nurse>> getById(@PathVariable int id) {
        Optional<Nurse> n = nurseService.findById(id);
        String msg = n.isPresent() ? "Nurse found" : "Nurse not found";
        return new ApiResponse<>(msg, n);
    }

    @GetMapping("/department/{department}")
    public ApiResponse<List<Nurse>> findByDepartment(@PathVariable String department) {
        List<Nurse> result = nurseService.findByDepartment(department);
        return new ApiResponse<>("Nurses in department: " + department, result);
    }

    @GetMapping("/sorted/name")
    public ApiResponse<List<Nurse>> listAllSortedByName() {
        return new ApiResponse<>("All nurses sorted by name",
                nurseService.getAllSortedByName());
    }
}
