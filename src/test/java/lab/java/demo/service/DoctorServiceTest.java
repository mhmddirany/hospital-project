package lab.java.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lab.java.demo.Models.Doctor;
import lab.java.demo.exception.DoctorConflictException;
import lab.java.demo.exception.DoctorNotFoundException;
import lab.java.demo.repository.DoctorRepository;

class DoctorServiceTest {

    private DoctorService doctorService;

    @BeforeEach
    void setUp() {
        doctorService = new DoctorService(new DoctorRepository());
    }

    @Test
    void addDoctorPersistsAndReturnsIt() {
        Doctor saved = doctorService.addDoctor(new Doctor(Doctor.nextId(), "Dr. Kim", 50, "Cardiology"));

        assertEquals("Cardiology", saved.getSpecialty());
        assertTrue(doctorService.findById(saved.getId()).isPresent());
    }

    @Test
    void addingADuplicateIdPropagatesTheConflict() {
        Doctor doctor = new Doctor(999_002, "Dr. Kim", 50, "Cardiology");
        doctorService.addDoctor(doctor);

        assertThrows(DoctorConflictException.class,
                () -> doctorService.addDoctor(new Doctor(999_002, "Dr. Someone Else", 40, "Neurology")));
    }

    @Test
    void getByIdOrThrowThrowsWhenMissing() {
        assertThrows(DoctorNotFoundException.class, () -> doctorService.getByIdOrThrow(404));
    }

    @Test
    void findBySpecialtyFiltersCorrectly() {
        doctorService.addDoctor(new Doctor(Doctor.nextId(), "Dr. Kim", 50, "Cardiology"));
        doctorService.addDoctor(new Doctor(Doctor.nextId(), "Dr. Patel", 45, "Neurology"));

        List<Doctor> cardiologists = doctorService.findBySpecialty("Cardiology");
        assertEquals(1, cardiologists.size());
        assertEquals("Dr. Kim", cardiologists.get(0).getName());
    }

    @Test
    void isAvailableReflectsTheDoctorsAvailabilityFlag() {
        // Issue 19: Receptionist.checkAvailability(doctorId) used to always
        // return true without looking at the doctor. This is the real
        // backing for that check.
        Doctor doctor = doctorService.addDoctor(new Doctor(Doctor.nextId(), "Dr. Kim", 50, "Cardiology"));
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
        doctorService.addDoctor(new Doctor(Doctor.nextId(), "Dr. Zed", 50, "Neurology"));
        doctorService.addDoctor(new Doctor(Doctor.nextId(), "Dr. Adams", 40, "Cardiology"));
        doctorService.addDoctor(new Doctor(Doctor.nextId(), "Dr. Baker", 42, "Cardiology"));

        List<Doctor> sorted = doctorService.getAllSortedBySpecialty();
        assertEquals("Cardiology", sorted.get(0).getSpecialty());
        assertEquals("Dr. Adams", sorted.get(0).getName());
        assertEquals("Cardiology", sorted.get(1).getSpecialty());
        assertEquals("Dr. Baker", sorted.get(1).getName());
        assertEquals("Neurology", sorted.get(2).getSpecialty());
    }
}
