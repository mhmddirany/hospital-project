package lab.java.demo.controller;

import lab.java.demo.Models.Nurse;
import lab.java.demo.dto.ApiResponse;
import lab.java.demo.dto.NurseRequest;
import lab.java.demo.service.NurseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nurses")
public class NurseController {

    private final NurseService nurseService;

    public NurseController(NurseService nurseService) {
        this.nurseService = nurseService;
    }

    @PostMapping
    public ApiResponse<Nurse> addNurse(@Valid @RequestBody NurseRequest request) {
        Nurse nurse = new Nurse(Nurse.nextId(), request.getName(), request.getAge(), request.getDepartment());
        Nurse saved = nurseService.addNurse(nurse);
        return new ApiResponse<>("Nurse added successfully", saved);
    }

    @GetMapping("/{id}")
    public ApiResponse<Nurse> getById(@PathVariable int id) {
        Nurse nurse = nurseService.getByIdOrThrow(id);
        return new ApiResponse<>("Nurse found", nurse);
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
