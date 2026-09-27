package lab.java.demo.repository;

import lab.java.demo.Models.Patient;
import lab.java.demo.exception.PatientConflictException;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class PatientRepository {

    private final Map<Integer, Patient> patients = new ConcurrentHashMap<>();

    public List<Patient> findAll() {
        return new ArrayList<>(patients.values());
    }

    public Optional<Patient> findById(int id) {
        return Optional.ofNullable(patients.get(id));
    }

    public Patient save(Patient patient) {
        if (patients.putIfAbsent(patient.getId(), patient) != null) {
            throw new PatientConflictException(patient.getId());
        }
        return patient;
    }

    public boolean deleteById(int id) {
        return patients.remove(id) != null;
    }

    public List<Patient> findByNameContainingIgnoreCase(String namePart) {
        String lower = namePart.toLowerCase();
        List<Patient> result = new ArrayList<>();
        for (Patient p : patients.values()) {
            if (p.getName().toLowerCase().contains(lower)) {
                result.add(p);
            }
        }
        return result;
    }
}
