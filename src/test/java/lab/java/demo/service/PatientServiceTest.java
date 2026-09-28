package lab.java.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lab.java.demo.Models.Patient;
import lab.java.demo.exception.PatientConflictException;
import lab.java.demo.exception.PatientNotFoundException;
import lab.java.demo.repository.PatientRepository;

class PatientServiceTest {

    private PatientService patientService;

    @BeforeEach
    void setUp() {
        patientService = new PatientService(new PatientRepository());
    }

    @Test
    void registerPatientPersistsAndReturnsIt() {
        Patient saved = patientService.registerPatient(new Patient(Patient.nextId(), "Alex Rivera", 40));

        assertEquals("Alex Rivera", saved.getName());
        assertTrue(patientService.findById(saved.getId()).isPresent());
    }

    @Test
    void registeringADuplicateIdPropagatesTheConflict() {
        Patient patient = new Patient(999_001, "Alex Rivera", 40);
        patientService.registerPatient(patient);

        assertThrows(PatientConflictException.class,
                () -> patientService.registerPatient(new Patient(999_001, "Someone Else", 25)));
    }

    @Test
    void getByIdOrThrowReturnsTheStoredPatient() {
        Patient saved = patientService.registerPatient(new Patient(Patient.nextId(), "Alex Rivera", 40));

        assertEquals(saved, patientService.getByIdOrThrow(saved.getId()));
    }

    @Test
    void getByIdOrThrowThrowsWhenMissing() {
        assertThrows(PatientNotFoundException.class, () -> patientService.getByIdOrThrow(404));
    }

    @Test
    void searchByNameFindsPartialCaseInsensitiveMatches() {
        patientService.registerPatient(new Patient(Patient.nextId(), "Alex Rivera", 40));
        patientService.registerPatient(new Patient(Patient.nextId(), "Blair Chen", 30));

        List<Patient> result = patientService.searchByName("riv");
        assertEquals(1, result.size());
        assertEquals("Alex Rivera", result.get(0).getName());
    }

    @Test
    void getAllSortedByNameOrdersAlphabetically() {
        patientService.registerPatient(new Patient(Patient.nextId(), "Zara Ahmed", 22));
        patientService.registerPatient(new Patient(Patient.nextId(), "Amir Saleh", 45));

        List<Patient> sorted = patientService.getAllSortedByName();
        int amirIndex = indexOfName(sorted, "Amir Saleh");
        int zaraIndex = indexOfName(sorted, "Zara Ahmed");
        assertTrue(amirIndex < zaraIndex);
    }

    @Test
    void getAllSortedByAgeDescendingOrdersOldestFirst() {
        patientService.registerPatient(new Patient(Patient.nextId(), "Young Patient", 20));
        patientService.registerPatient(new Patient(Patient.nextId(), "Old Patient", 90));

        List<Patient> sorted = patientService.getAllSortedByAgeDescending();
        int oldIndex = indexOfName(sorted, "Old Patient");
        int youngIndex = indexOfName(sorted, "Young Patient");
        assertTrue(oldIndex < youngIndex);
    }

    private int indexOfName(List<Patient> patients, String name) {
        for (int i = 0; i < patients.size(); i++) {
            if (patients.get(i).getName().equals(name)) {
                return i;
            }
        }
        throw new AssertionError("Expected to find patient named " + name);
    }
}
