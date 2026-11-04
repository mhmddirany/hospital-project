package lab.java.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import lab.java.demo.Models.Patient;

/**
 * Step 1: a Spring Data JPA repository backed by the database, replacing
 * the old ConcurrentHashMap. JpaRepository already provides save, findById,
 * findAll, existsById, deleteById, count and paging; Spring writes the SQL
 * for each extra method below from its name (a "derived query").
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Integer> {

    /** WHERE lower(name) LIKE lower('%' || :namePart || '%') */
    List<Patient> findByNameContainingIgnoreCase(String namePart);
}
