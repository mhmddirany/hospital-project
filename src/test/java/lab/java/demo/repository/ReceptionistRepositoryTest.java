package lab.java.demo.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lab.java.demo.Models.Receptionist;
import lab.java.demo.exception.ReceptionistConflictException;

class ReceptionistRepositoryTest {

    private ReceptionistRepository repository;

    @BeforeEach
    void setUp() {
        repository = new ReceptionistRepository();
    }

    @Test
    void savesAndFindsById() {
        Receptionist saved = repository.save(new Receptionist(1, "Rana Youssef", 27));

        Optional<Receptionist> found = repository.findById(1);
        assertTrue(found.isPresent());
        assertEquals(saved, found.get());
        assertEquals("Rana Youssef", found.get().getName());
    }

    @Test
    void findByIdReturnsEmptyWhenMissing() {
        assertTrue(repository.findById(404).isEmpty());
    }

    @Test
    void savingADuplicateIdThrowsConflict() {
        repository.save(new Receptionist(1, "Rana Youssef", 27));

        assertThrows(ReceptionistConflictException.class,
                () -> repository.save(new Receptionist(1, "Someone Else", 31)));

        assertEquals("Rana Youssef", repository.findById(1).orElseThrow().getName());
    }

    @Test
    void findAllReturnsEveryReceptionist() {
        repository.save(new Receptionist(1, "Rana Youssef", 27));
        repository.save(new Receptionist(2, "Samir Aziz", 33));

        assertEquals(2, repository.findAll().size());
    }

    @Test
    void deleteByIdRemovesTheRecordAndReportsWhetherAnythingWasRemoved() {
        repository.save(new Receptionist(1, "Rana Youssef", 27));

        assertTrue(repository.deleteById(1));
        assertFalse(repository.deleteById(1));
    }
}
