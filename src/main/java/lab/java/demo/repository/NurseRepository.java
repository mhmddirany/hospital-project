package lab.java.demo.repository;

import lab.java.demo.Models.Nurse;
import lab.java.demo.exception.NurseConflictException;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class NurseRepository {

    private final Map<Integer, Nurse> nurses = new ConcurrentHashMap<>();

    public List<Nurse> findAll() {
        return new ArrayList<>(nurses.values());
    }

    public Optional<Nurse> findById(int id) {
        return Optional.ofNullable(nurses.get(id));
    }

    public Nurse save(Nurse nurse) {
        if (nurses.putIfAbsent(nurse.getId(), nurse) != null) {
            throw new NurseConflictException(nurse.getId());
        }
        return nurse;
    }

    public boolean deleteById(int id) {
        return nurses.remove(id) != null;
    }

    public List<Nurse> findByDepartment(String department) {
        List<Nurse> result = new ArrayList<>();
        for (Nurse n : nurses.values()) {
            if (department.equalsIgnoreCase(n.getDepartment())) {
                result.add(n);
            }
        }
        return result;
    }
}
