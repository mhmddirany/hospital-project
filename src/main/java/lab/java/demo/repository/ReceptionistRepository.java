package lab.java.demo.repository;

import lab.java.demo.Models.Receptionist;
import lab.java.demo.exception.ReceptionistConflictException;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ReceptionistRepository {

    private final Map<Integer, Receptionist> receptionists = new ConcurrentHashMap<>();

    public List<Receptionist> findAll() {
        return new ArrayList<>(receptionists.values());
    }

    public Optional<Receptionist> findById(int id) {
        return Optional.ofNullable(receptionists.get(id));
    }

    public Receptionist save(Receptionist receptionist) {
        if (receptionists.putIfAbsent(receptionist.getId(), receptionist) != null) {
            throw new ReceptionistConflictException(receptionist.getId());
        }
        return receptionist;
    }

    public boolean deleteById(int id) {
        return receptionists.remove(id) != null;
    }
}
