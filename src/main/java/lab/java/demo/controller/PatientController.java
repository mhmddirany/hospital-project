package lab.java.demo.controller;

import lab.java.demo.Models.Patient;
import lab.java.demo.dto.ApiResponse;
import lab.java.demo.dto.PageResponse;
import lab.java.demo.dto.PatientRequest;
import lab.java.demo.dto.PatientResponse;
import lab.java.demo.service.PatientService;
import lab.java.demo.util.PageRequests;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    /** Step 2: the only fields a client may sort patient lists by. */
    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "name", "age");

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    public ApiResponse<PatientResponse> register(@Valid @RequestBody PatientRequest request) {
        Patient patient = new Patient(request.getName(), request.getAge());
        // Issue 16 follow-up: optional contact info, so a real
        // email/SMS reminder provider has somewhere to send to.
        patient.setEmail(request.getEmail());
        patient.setPhone(request.getPhone());
        Patient saved = patientService.registerPatient(patient);
        return new ApiResponse<>("Patient registered successfully", PatientResponse.from(saved));
    }

    /**
     * Step 2: GET /api/patients?name=riv&page=0&size=20&sort=age,desc
     *
     * <p>Every parameter is optional. Spring builds the {@link Pageable}
     * from {@code page}, {@code size} and {@code sort} automatically;
     * {@code @PageableDefault} supplies the values when they're missing.
     * Replaces /search/{name} and /sorted/* for new clients -- those are
     * kept for now so nothing that already calls them breaks.
     */
    @GetMapping
    public ApiResponse<PageResponse<PatientResponse>> list(
            @RequestParam(required = false) String name,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<Patient> page = patientService.findPage(name, PageRequests.safe(pageable, SORTABLE_FIELDS));
        return new ApiResponse<>("Patients", PageResponse.from(page, PatientResponse::from));
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
