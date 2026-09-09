package lab.java.demo.repository;

import lab.java.demo.Models.Receptionist;

import java.util.*;

public class ReceptionistRepository {

    private final Map<Integer, Receptionist> receptionists = new HashMap<>();

    public List<Receptionist> findAll() {
        return new ArrayList<>(receptionists.values());
    }

    public Optional<Receptionist> findById(int id) {
        return Optional.ofNullable(receptionists.get(id));
    }

    public Receptionist save(Receptionist receptionist) {
        receptionists.put(receptionist.getId(), receptionist);
        return receptionist;
    }

    public boolean deleteById(int id) {
        return receptionists.remove(id) != null;
    }
}
