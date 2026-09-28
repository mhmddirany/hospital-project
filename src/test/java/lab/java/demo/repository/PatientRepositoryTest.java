package lab.java.demo.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lab.java.demo.Models.Patient;
import lab.java.demo.exception.PatientConflictException;

/**
 * Issue 24: the repositories had no tests at all. PatientRepository is a
 * hand-rolled in-memory class with no Spring-specific behavior, so it's
 * constructed directly here rather than through a Spring context.
 */
class PatientRepositoryTest {

    private PatientRepository repository;

    @BeforeEach
    void setUp() {
        repository = new PatientRepository();
    }

    @Test
    void savesAndFindsById() {
        Patient saved = repository.save(new Patient(1, "Alex Rivera", 40));

        Optional<Patient> found = repository.findById(1);
        assertTrue(found.isPresent());
        assertEquals(saved, found.get());
        assertEquals("Alex Rivera", found.get().getName());
    }

    @Test
    void findByIdReturnsEmptyWhenMissing() {
        assertTrue(repository.findById(404).isEmpty());
    }

    @Test
    void savingADuplicateIdThrowsConflict() {
        // Issue 8: saving an id that already exists must not silently
        // overwrite the original record.
        repository.save(new Patient(1, "Alex Rivera", 40));

        PatientConflictException ex = assertThrows(PatientConflictException.class,
                () -> repository.save(new Patient(1, "Someone Else", 25)));
        assertTrue(ex.getMessage().contains("1"));

        // The original record must still be intact.
        assertEquals("Alex Rivera", repository.findById(1).orElseThrow().getName());
    }

    @Test
    void findAllReturnsEverySavedPatient() {
        repository.save(new Patient(1, "Alex Rivera", 40));
        repository.save(new Patient(2, "Blair Chen", 30));

        List<Patient> all = repository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void findByNameContainingIgnoreCaseMatchesPartialAndCase() {
        repository.save(new Patient(1, "Alex Rivera", 40));
        repository.save(new Patient(2, "Blair Chen", 30));

        List<Patient> matches = repository.findByNameContainingIgnoreCase("riv");
        assertEquals(1, matches.size());
        assertEquals("Alex Rivera", matches.get(0).getName());

        assertTrue(repository.findByNameContainingIgnoreCase("zzz").isEmpty());
    }

    @Test
    void deleteByIdRemovesTheRecordAndReportsWhetherAnythingWasRemoved() {
        repository.save(new Patient(1, "Alex Rivera", 40));

        assertTrue(repository.deleteById(1));
        assertTrue(repository.findById(1).isEmpty());
        assertFalse(repository.deleteById(1));
    }
}
