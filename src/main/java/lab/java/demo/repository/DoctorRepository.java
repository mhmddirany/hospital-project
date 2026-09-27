package lab.java.demo.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import lab.java.demo.Models.Doctor;
import lab.java.demo.exception.DoctorConflictException;

/**
 * Repository for Doctor objects.
 */
@Repository
public class DoctorRepository {

    private final Map<Integer, Doctor> doctors = new ConcurrentHashMap<>();

    public List<Doctor> findAll() {
        return new ArrayList<>(doctors.values());
    }

    public Optional<Doctor> findById(int id) {
        return Optional.ofNullable(doctors.get(id));
    }

    public Doctor save(Doctor doctor) {
        if (doctors.putIfAbsent(doctor.getId(), doctor) != null) {
            throw new DoctorConflictException(doctor.getId());
        }
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
