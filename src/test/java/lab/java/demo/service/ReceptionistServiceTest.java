package lab.java.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lab.java.demo.Models.Receptionist;
import lab.java.demo.exception.ReceptionistConflictException;
import lab.java.demo.exception.ReceptionistNotFoundException;
import lab.java.demo.repository.ReceptionistRepository;

class ReceptionistServiceTest {

    private ReceptionistService receptionistService;

    @BeforeEach
    void setUp() {
        receptionistService = new ReceptionistService(new ReceptionistRepository());
    }

    @Test
    void addReceptionistPersistsAndReturnsIt() {
        Receptionist saved = receptionistService.addReceptionist(new Receptionist(Receptionist.nextId(), "Rana Youssef", 27));

        assertEquals("Rana Youssef", saved.getName());
        assertTrue(receptionistService.findById(saved.getId()).isPresent());
    }

    @Test
    void addingADuplicateIdPropagatesTheConflict() {
        Receptionist receptionist = new Receptionist(999_004, "Rana Youssef", 27);
        receptionistService.addReceptionist(receptionist);

        assertThrows(ReceptionistConflictException.class,
                () -> receptionistService.addReceptionist(new Receptionist(999_004, "Someone Else", 31)));
    }

    @Test
    void getByIdOrThrowThrowsWhenMissing() {
        assertThrows(ReceptionistNotFoundException.class, () -> receptionistService.getByIdOrThrow(404));
    }

    @Test
    void getAllSortedByNameOrdersAlphabetically() {
        receptionistService.addReceptionist(new Receptionist(Receptionist.nextId(), "Zara Ahmed", 22));
        receptionistService.addReceptionist(new Receptionist(Receptionist.nextId(), "Amir Saleh", 45));

        List<Receptionist> sorted = receptionistService.getAllSortedByName();
        assertEquals("Amir Saleh", sorted.get(0).getName());
        assertEquals("Zara Ahmed", sorted.get(1).getName());
    }
}
