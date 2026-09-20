package lab.java.demo.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import lab.java.demo.Models.Doctor;

/**
 * Repository for Doctor objects.
 */
@Repository
public class DoctorRepository {

    private final Map<Integer, Doctor> doctors = new HashMap<>();

    public List<Doctor> findAll() {
        return new ArrayList<>(doctors.values());
    }

    public Optional<Doctor> findById(int id) {
        return Optional.ofNullable(doctors.get(id));
    }

    public Doctor save(Doctor doctor) {
        doctors.put(doctor.getId(), doctor);
        return doctor;
    }

    public boolean deleteById(int id) {
        return doctors.remove(id) != null;
    }

    public List<Doctor> findBySpecialty(String specialty) {
        List<Doctor> result = new ArrayList<>();
        for (Doctor d : doctors.values()) {
            if (specialty.equalsIgnoreCase(d.getSpecialty())) {
                result.add(d);
            }
        }
        return result;
    }
}
