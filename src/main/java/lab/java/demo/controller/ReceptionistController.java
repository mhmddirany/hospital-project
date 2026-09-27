package lab.java.demo.controller;

import lab.java.demo.Models.Receptionist;
import lab.java.demo.dto.ApiResponse;
import lab.java.demo.dto.ReceptionistRequest;
import lab.java.demo.service.ReceptionistService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/receptionists")
public class ReceptionistController {

    private final ReceptionistService receptionistService;

    public ReceptionistController(ReceptionistService receptionistService) {
        this.receptionistService = receptionistService;
    }

    @PostMapping
    public ApiResponse<Receptionist> addReceptionist(@Valid @RequestBody ReceptionistRequest request) {
        Receptionist receptionist = new Receptionist(Receptionist.nextId(), request.getName(), request.getAge());
        Receptionist saved = receptionistService.addReceptionist(receptionist);
        return new ApiResponse<>("Receptionist added successfully", saved);
    }

    @GetMapping("/{id}")
    public ApiResponse<Receptionist> getById(@PathVariable int id) {
        Receptionist receptionist = receptionistService.getByIdOrThrow(id);
        return new ApiResponse<>("Receptionist found", receptionist);
    }

    @GetMapping("/sorted/name")
    public ApiResponse<List<Receptionist>> listAllSortedByName() {
        return new ApiResponse<>("All receptionists sorted by name",
                receptionistService.getAllSortedByName());
    }
}
