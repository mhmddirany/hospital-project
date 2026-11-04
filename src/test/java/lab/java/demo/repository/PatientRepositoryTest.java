package lab.java.demo.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import lab.java.demo.Models.*;

/**
 * Step 1: a {@code @DataJpaTest} starts only the JPA layer (entities,
 * repositories, Flyway) against the test database -- no web layer, no
 * security. Each test runs in a transaction that is rolled back at the
 * end, so tests never see each other's data.
 *
 * <p>{@code replace = NONE} keeps the H2-in-PostgreSQL-mode database from
 * application-test.properties instead of Spring's default embedded one.
 * {@code flushAndClear()} forces a real INSERT and makes the next read go
 * back to the database, so these tests check what was actually stored,
 * not just objects sitting in Hibernate's cache.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PatientRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private PatientRepository repository;

    @Test
    void saveGeneratesAnIdAndTheRowCanBeReadBack() {
        Patient saved = repository.save(new Patient("Alex Rivera", 40));
        assertNotNull(saved.getId());
        flushAndClear();

        Patient found = repository.findById(saved.getId()).orElseThrow();
        assertEquals("Alex Rivera", found.getName());
        assertEquals(40, found.getAge());
        assertEquals(saved, found);
    }

    @Test
    void idsAreGeneratedByTheDatabaseAndNeverReused() {
        // Issue 8 is now enforced by the database: ids are never client
        // supplied, so there is no duplicate id to reject.
        Patient a = repository.save(new Patient("Alex Rivera", 40));
        Patient b = repository.save(new Patient("Blair Chen", 30));
        assertNotEquals(a.getId(), b.getId());
    }

    @Test
    void findByIdReturnsEmptyWhenMissing() {
        assertTrue(repository.findById(404_404).isEmpty());
    }

    @Test
    void contactInfoIsStored() {
        Patient p = new Patient("Alex Rivera", 40);
        p.setEmail("alex@example.com");
        p.setPhone("+961 1 234 567");
        Integer id = repository.save(p).getId();
        flushAndClear();

        Patient found = repository.findById(id).orElseThrow();
        assertEquals("alex@example.com", found.getEmail());
        assertEquals("+961 1 234 567", found.getPhone());
    }

    @Test
    void medicalRecordConditionsAndMedicationsAreStoredInOrder() {
        Patient p = new Patient("Alex Rivera", 40);
        p.getMedicalRecord().setAllergies("penicillin");
        p.getMedicalRecord().addCondition("asthma");
        p.getMedicalRecord().addCondition("hypertension");
        p.getMedicalRecord().addMedication("salbutamol");
        Integer id = repository.save(p).getId();
        flushAndClear();

        MedicalRecord record = repository.findById(id).orElseThrow().getMedicalRecord();
        assertEquals("penicillin", record.getAllergies());
        assertEquals(List.of("asthma", "hypertension"), record.getConditions());
        assertEquals(List.of("salbutamol"), record.getMedications());
    }

    @Test
    void patientWithAnEmptyMedicalRecordNeverReturnsNull() {
        Integer id = repository.save(new Patient("Alex Rivera", 40)).getId();
        flushAndClear();
        assertNotNull(repository.findById(id).orElseThrow().getMedicalRecord());
    }

    @Test
    void findByNameContainingIgnoreCaseMatchesPartialAndCase() {
        repository.save(new Patient("Alex Rivera", 40));
        repository.save(new Patient("Blair Chen", 30));
        flushAndClear();

        List<Patient> matches = repository.findByNameContainingIgnoreCase("RIV");
        assertEquals(1, matches.size());
        assertEquals("Alex Rivera", matches.get(0).getName());
        assertTrue(repository.findByNameContainingIgnoreCase("zzz").isEmpty());
    }

    @Test
    void deleteByIdRemovesTheRow() {
        Integer id = repository.save(new Patient("Alex Rivera", 40)).getId();
        repository.deleteById(id);
        flushAndClear();
        assertFalse(repository.existsById(id));
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}
