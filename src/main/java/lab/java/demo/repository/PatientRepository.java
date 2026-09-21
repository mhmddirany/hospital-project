package lab.java.demo.repository;

import lab.java.demo.Models.Patient;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class PatientRepository {

    private final Map<Integer, Patient> patients = new HashMap<>();

    public List<Patient> findAll() {
        return new ArrayList<>(patients.values());
    }

    public Optional<Patient> findById(int id) {
        return Optional.ofNullable(patients.get(id));
    }

    public Patient save(Patient patient) {
        patients.put(patient.getId(), patient);
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
