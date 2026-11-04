package lab.java.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import lab.java.demo.Models.Doctor;
import lab.java.demo.exception.DoctorNotFoundException;
import lab.java.demo.repository.DoctorRepository;

/**
 * Step 1: runs against the real JPA repositories and the test database
 * ({@code @DataJpaTest}), with the service itself still constructed by
 * hand -- the test stays a focused service test, but every save and query
 * now goes through real SQL. Each test is rolled back at the end.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DoctorServiceTest {

    @Autowired
    private DoctorRepository doctorRepository;

    private DoctorService doctorService;

    @BeforeEach
    void setUp() {
        doctorService = new DoctorService(doctorRepository);
    }

    @Test
    void addDoctorPersistsAndReturnsIt() {
        Doctor saved = doctorService.addDoctor(new Doctor("Dr. Kim", 50, "Cardiology"));

        assertEquals("Cardiology", saved.getSpecialty());
        assertTrue(doctorService.findById(saved.getId()).isPresent());
    }


    @Test
    void getByIdOrThrowThrowsWhenMissing() {
        assertThrows(DoctorNotFoundException.class, () -> doctorService.getByIdOrThrow(404));
    }

    @Test
    void findBySpecialtyFiltersCorrectly() {
        doctorService.addDoctor(new Doctor("Dr. Kim", 50, "Cardiology"));
        doctorService.addDoctor(new Doctor("Dr. Patel", 45, "Neurology"));

        List<Doctor> cardiologists = doctorService.findBySpecialty("Cardiology");
        assertEquals(1, cardiologists.size());
        assertEquals("Dr. Kim", cardiologists.get(0).getName());
    }

    @Test
    void isAvailableReflectsTheDoctorsAvailabilityFlag() {
        // Issue 19: Receptionist.checkAvailability(doctorId) used to always
        // return true without looking at the doctor. This is the real
        // backing for that check.
        Doctor doctor = doctorService.addDoctor(new Doctor("Dr. Kim", 50, "Cardiology"));
        assertTrue(doctorService.isAvailable(doctor.getId()));

        doctor.setAvailability(false);
        assertFalse(doctorService.isAvailable(doctor.getId()));
    }

    @Test
    void isAvailableThrowsWhenTheDoctorDoesNotExist() {
        assertThrows(DoctorNotFoundException.class, () -> doctorService.isAvailable(404));
    }

    @Test
    void getAllSortedBySpecialtyGroupsBySpecialtyThenName() {
        doctorService.addDoctor(new Doctor("Dr. Zed", 50, "Neurology"));
        doctorService.addDoctor(new Doctor("Dr. Adams", 40, "Cardiology"));
        doctorService.addDoctor(new Doctor("Dr. Baker", 42, "Cardiology"));

        List<Doctor> sorted = doctorService.getAllSortedBySpecialty();
        assertEquals("Cardiology", sorted.get(0).getSpecialty());
        assertEquals("Dr. Adams", sorted.get(0).getName());
        assertEquals("Cardiology", sorted.get(1).getSpecialty());
        assertEquals("Dr. Baker", sorted.get(1).getName());
        assertEquals("Neurology", sorted.get(2).getSpecialty());
    }
}
