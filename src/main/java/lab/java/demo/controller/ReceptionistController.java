package lab.java.demo.controller;

import lab.java.demo.Models.Receptionist;
import lab.java.demo.dto.ApiResponse;
import lab.java.demo.service.ReceptionistService;

import java.util.List;
import java.util.Optional;

public class ReceptionistController {

    private final ReceptionistService receptionistService;

    public ReceptionistController(ReceptionistService receptionistService) {
        this.receptionistService = receptionistService;
    }

    public ApiResponse<Receptionist> addReceptionist(Receptionist r) {
        Receptionist saved = receptionistService.addReceptionist(r);
        return new ApiResponse<>("Receptionist added successfully", saved);
    }

    public ApiResponse<Optional<Receptionist>> getById(int id) {
        Optional<Receptionist> r = receptionistService.findById(id);
        String msg = r.isPresent() ? "Receptionist found" : "Receptionist not found";
        return new ApiResponse<>(msg, r);
    }

    public ApiResponse<List<Receptionist>> listAllSortedByName() {
        return new ApiResponse<>("All receptionists sorted by name",
                receptionistService.getAllSortedByName());
    }
}
