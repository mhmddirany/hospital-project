package lab.java.demo.service;

import lab.java.demo.Models.Patient;
import lab.java.demo.exception.PatientNotFoundException;
import lab.java.demo.repository.PatientRepository;
import lab.java.demo.util.PatientComparators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    private static final Logger log = LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public Patient registerPatient(Patient patient) {
        // Issue 22: this used to log the patient's name and age (PII) on
        // every registration. Logs are identifiers-only now -- the id is
        // enough to correlate this line with the saved record, and a full
        // name/age doesn't belong in an operational log that may be
        // aggregated, retained, or read by anyone with log access.
        log.info("Registering patient id={}", patient.getId());
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
        // Issue 22: the search term is itself patient-identifying (it's
        // often literally a patient's name), so it's left out of the log
        // line even at DEBUG -- the match count is enough to debug this.
        log.debug("Found {} patients matching a name search", result.size());
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
