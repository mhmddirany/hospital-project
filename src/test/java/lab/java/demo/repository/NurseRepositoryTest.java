package lab.java.demo.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lab.java.demo.Models.Nurse;
import lab.java.demo.exception.NurseConflictException;

class NurseRepositoryTest {

    private NurseRepository repository;

    @BeforeEach
    void setUp() {
        repository = new NurseRepository();
    }

    @Test
    void savesAndFindsById() {
        Nurse saved = repository.save(new Nurse(1, "Nadia Cruz", 35, "ICU"));

        Optional<Nurse> found = repository.findById(1);
        assertTrue(found.isPresent());
        assertEquals(saved, found.get());
        assertEquals("ICU", found.get().getDepartment());
    }

    @Test
    void findByIdReturnsEmptyWhenMissing() {
        assertTrue(repository.findById(404).isEmpty());
    }

    @Test
    void savingADuplicateIdThrowsConflict() {
        repository.save(new Nurse(1, "Nadia Cruz", 35, "ICU"));

        assertThrows(NurseConflictException.class,
                () -> repository.save(new Nurse(1, "Someone Else", 28, "ER")));

        assertEquals("ICU", repository.findById(1).orElseThrow().getDepartment());
    }

    @Test
    void findAllReturnsEveryNurse() {
        repository.save(new Nurse(1, "Nadia Cruz", 35, "ICU"));
        repository.save(new Nurse(2, "Omar Haddad", 29, "ER"));

        assertEquals(2, repository.findAll().size());
    }

    @Test
    void findByDepartmentIsCaseInsensitiveAndExact() {
        repository.save(new Nurse(1, "Nadia Cruz", 35, "ICU"));
        repository.save(new Nurse(2, "Omar Haddad", 29, "ER"));

        List<Nurse> icuNurses = repository.findByDepartment("icu");
        assertEquals(1, icuNurses.size());
        assertEquals("Nadia Cruz", icuNurses.get(0).getName());

        assertTrue(repository.findByDepartment("Oncology").isEmpty());
    }

    @Test
    void deleteByIdRemovesTheRecordAndReportsWhetherAnythingWasRemoved() {
        repository.save(new Nurse(1, "Nadia Cruz", 35, "ICU"));

        assertTrue(repository.deleteById(1));
        assertFalse(repository.deleteById(1));
    }
}
