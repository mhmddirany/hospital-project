package lab.java.demo.repository;

import lab.java.demo.Models.Nurse;

import java.util.*;

public class NurseRepository {

    private final Map<Integer, Nurse> nurses = new HashMap<>();

    public List<Nurse> findAll() {
        return new ArrayList<>(nurses.values());
    }

    public Optional<Nurse> findById(int id) {
        return Optional.ofNullable(nurses.get(id));
    }

    public Nurse save(Nurse nurse) {
        nurses.put(nurse.getId(), nurse);
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
