package lab.java.demo.service;

import lab.java.demo.Models.Patient;
import lab.java.demo.exception.PatientNotFoundException;
import lab.java.demo.repository.PatientRepository;
import lab.java.demo.util.PatientComparators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class PatientService {

    private static final Logger log = LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public Patient registerPatient(Patient patient) {
        log.info("Registering patient name={} age={}", patient.getName(), patient.getAge());
        return patientRepository.save(patient);
    }

    public Optional<Patient> findById(int id) {
        return patientRepository.findById(id);
    }

    public Patient getByIdOrThrow(int id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Patient with id={} not found", id);
                    return new PatientNotFoundException(id);
                });
    }

    public List<Patient> searchByName(String partialName) {
        List<Patient> result = patientRepository.findByNameContainingIgnoreCase(partialName);
        log.debug("Found {} patients matching name '{}'", result.size(), partialName);
        return result;
    }

    public List<Patient> getAllSortedByName() {
        List<Patient> list = patientRepository.findAll();
        list.sort(PatientComparators.byName());
        log.debug("Returning {} patients sorted by name", list.size());
        return list;
    }

    public List<Patient> getAllSortedByAgeDescending() {
        List<Patient> list = patientRepository.findAll();
        list.sort(PatientComparators.byAgeDescending());
        log.debug("Returning {} patients sorted by age descending", list.size());
        return list;
    }
}
