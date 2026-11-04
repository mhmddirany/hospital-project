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

import lab.java.demo.Models.Nurse;
import lab.java.demo.exception.NurseNotFoundException;
import lab.java.demo.repository.NurseRepository;

/**
 * Step 1: runs against the real JPA repositories and the test database
 * ({@code @DataJpaTest}), with the service itself still constructed by
 * hand -- the test stays a focused service test, but every save and query
 * now goes through real SQL. Each test is rolled back at the end.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NurseServiceTest {

    @Autowired
    private NurseRepository nurseRepository;

    private NurseService nurseService;

    @BeforeEach
    void setUp() {
        nurseService = new NurseService(nurseRepository);
    }

    @Test
    void addNursePersistsAndReturnsIt() {
        Nurse saved = nurseService.addNurse(new Nurse("Nadia Cruz", 35, "ICU"));

        assertEquals("ICU", saved.getDepartment());
        assertTrue(nurseService.findById(saved.getId()).isPresent());
    }


    @Test
    void getByIdOrThrowThrowsWhenMissing() {
        assertThrows(NurseNotFoundException.class, () -> nurseService.getByIdOrThrow(404));
    }

    @Test
    void findByDepartmentFiltersCorrectly() {
        nurseService.addNurse(new Nurse("Nadia Cruz", 35, "ICU"));
        nurseService.addNurse(new Nurse("Omar Haddad", 29, "ER"));

        List<Nurse> icuNurses = nurseService.findByDepartment("ICU");
        assertEquals(1, icuNurses.size());
        assertEquals("Nadia Cruz", icuNurses.get(0).getName());
    }

    @Test
    void getAllSortedByNameOrdersAlphabetically() {
        nurseService.addNurse(new Nurse("Zara Ahmed", 22, "ER"));
        nurseService.addNurse(new Nurse("Amir Saleh", 45, "ICU"));

        List<Nurse> sorted = nurseService.getAllSortedByName();
        assertEquals("Amir Saleh", sorted.get(0).getName());
        assertEquals("Zara Ahmed", sorted.get(1).getName());
    }
}
