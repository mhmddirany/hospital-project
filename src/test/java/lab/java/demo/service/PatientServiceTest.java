package lab.java.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import lab.java.demo.Models.Patient;
import lab.java.demo.exception.PatientNotFoundException;
import lab.java.demo.repository.PatientRepository;

/**
 * Step 1: runs against the real JPA repositories and the test database
 * ({@code @DataJpaTest}), with the service itself still constructed by
 * hand -- the test stays a focused service test, but every save and query
 * now goes through real SQL. Each test is rolled back at the end.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PatientServiceTest {

    @Autowired
    private PatientRepository patientRepository;

    private PatientService patientService;

    @BeforeEach
    void setUp() {
        patientService = new PatientService(patientRepository);
    }

    @Test
    void registerPatientPersistsAndReturnsIt() {
        Patient saved = patientService.registerPatient(new Patient("Alex Rivera", 40));

        assertEquals("Alex Rivera", saved.getName());
        assertTrue(patientService.findById(saved.getId()).isPresent());
    }


    @Test
    void getByIdOrThrowReturnsTheStoredPatient() {
        Patient saved = patientService.registerPatient(new Patient("Alex Rivera", 40));

        assertEquals(saved, patientService.getByIdOrThrow(saved.getId()));
    }

    @Test
    void getByIdOrThrowThrowsWhenMissing() {
        assertThrows(PatientNotFoundException.class, () -> patientService.getByIdOrThrow(404));
    }

    @Test
    void searchByNameFindsPartialCaseInsensitiveMatches() {
        patientService.registerPatient(new Patient("Alex Rivera", 40));
        patientService.registerPatient(new Patient("Blair Chen", 30));

        List<Patient> result = patientService.searchByName("riv");
        assertEquals(1, result.size());
        assertEquals("Alex Rivera", result.get(0).getName());
    }

    @Test
    void getAllSortedByNameOrdersAlphabetically() {
        patientService.registerPatient(new Patient("Zara Ahmed", 22));
        patientService.registerPatient(new Patient("Amir Saleh", 45));

        List<Patient> sorted = patientService.getAllSortedByName();
        int amirIndex = indexOfName(sorted, "Amir Saleh");
        int zaraIndex = indexOfName(sorted, "Zara Ahmed");
        assertTrue(amirIndex < zaraIndex);
    }

    @Test
    void getAllSortedByAgeDescendingOrdersOldestFirst() {
        patientService.registerPatient(new Patient("Young Patient", 20));
        patientService.registerPatient(new Patient("Old Patient", 90));

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
