package lab.java.demo.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lab.java.demo.Models.Doctor;
import lab.java.demo.exception.DoctorConflictException;

class DoctorRepositoryTest {

    private DoctorRepository repository;

    @BeforeEach
    void setUp() {
        repository = new DoctorRepository();
    }

    @Test
    void savesAndFindsById() {
        Doctor saved = repository.save(new Doctor(1, "Dr. Kim", 50, "Cardiology"));

        Optional<Doctor> found = repository.findById(1);
        assertTrue(found.isPresent());
        assertEquals(saved, found.get());
        assertEquals("Cardiology", found.get().getSpecialty());
    }

    @Test
    void findByIdReturnsEmptyWhenMissing() {
        assertTrue(repository.findById(404).isEmpty());
    }

    @Test
    void savingADuplicateIdThrowsConflict() {
        repository.save(new Doctor(1, "Dr. Kim", 50, "Cardiology"));

        assertThrows(DoctorConflictException.class,
                () -> repository.save(new Doctor(1, "Dr. Someone Else", 40, "Neurology")));

        assertEquals("Cardiology", repository.findById(1).orElseThrow().getSpecialty());
    }

    @Test
    void findAllReturnsEveryDoctor() {
        repository.save(new Doctor(1, "Dr. Kim", 50, "Cardiology"));
        repository.save(new Doctor(2, "Dr. Patel", 45, "Neurology"));

        assertEquals(2, repository.findAll().size());
    }

    @Test
    void findBySpecialtyIsCaseInsensitiveAndExact() {
        repository.save(new Doctor(1, "Dr. Kim", 50, "Cardiology"));
        repository.save(new Doctor(2, "Dr. Patel", 45, "Neurology"));

        List<Doctor> cardiologists = repository.findBySpecialty("cardiology");
        assertEquals(1, cardiologists.size());
        assertEquals("Dr. Kim", cardiologists.get(0).getName());

        assertTrue(repository.findBySpecialty("Oncology").isEmpty());
    }

    @Test
    void deleteByIdRemovesTheRecordAndReportsWhetherAnythingWasRemoved() {
        repository.save(new Doctor(1, "Dr. Kim", 50, "Cardiology"));

        assertTrue(repository.deleteById(1));
        assertFalse(repository.deleteById(1));
    }
}
