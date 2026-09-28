package lab.java.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lab.java.demo.Models.Nurse;
import lab.java.demo.exception.NurseConflictException;
import lab.java.demo.exception.NurseNotFoundException;
import lab.java.demo.repository.NurseRepository;

class NurseServiceTest {

    private NurseService nurseService;

    @BeforeEach
    void setUp() {
        nurseService = new NurseService(new NurseRepository());
    }

    @Test
    void addNursePersistsAndReturnsIt() {
        Nurse saved = nurseService.addNurse(new Nurse(Nurse.nextId(), "Nadia Cruz", 35, "ICU"));

        assertEquals("ICU", saved.getDepartment());
        assertTrue(nurseService.findById(saved.getId()).isPresent());
    }

    @Test
    void addingADuplicateIdPropagatesTheConflict() {
        Nurse nurse = new Nurse(999_003, "Nadia Cruz", 35, "ICU");
        nurseService.addNurse(nurse);

        assertThrows(NurseConflictException.class,
                () -> nurseService.addNurse(new Nurse(999_003, "Someone Else", 28, "ER")));
    }

    @Test
    void getByIdOrThrowThrowsWhenMissing() {
        assertThrows(NurseNotFoundException.class, () -> nurseService.getByIdOrThrow(404));
    }

    @Test
    void findByDepartmentFiltersCorrectly() {
        nurseService.addNurse(new Nurse(Nurse.nextId(), "Nadia Cruz", 35, "ICU"));
        nurseService.addNurse(new Nurse(Nurse.nextId(), "Omar Haddad", 29, "ER"));

        List<Nurse> icuNurses = nurseService.findByDepartment("ICU");
        assertEquals(1, icuNurses.size());
        assertEquals("Nadia Cruz", icuNurses.get(0).getName());
    }

    @Test
    void getAllSortedByNameOrdersAlphabetically() {
        nurseService.addNurse(new Nurse(Nurse.nextId(), "Zara Ahmed", 22, "ER"));
        nurseService.addNurse(new Nurse(Nurse.nextId(), "Amir Saleh", 45, "ICU"));

        List<Nurse> sorted = nurseService.getAllSortedByName();
        assertEquals("Amir Saleh", sorted.get(0).getName());
        assertEquals("Zara Ahmed", sorted.get(1).getName());
    }
}
