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

import lab.java.demo.Models.Receptionist;
import lab.java.demo.exception.ReceptionistNotFoundException;
import lab.java.demo.repository.ReceptionistRepository;

/**
 * Step 1: runs against the real JPA repositories and the test database
 * ({@code @DataJpaTest}), with the service itself still constructed by
 * hand -- the test stays a focused service test, but every save and query
 * now goes through real SQL. Each test is rolled back at the end.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReceptionistServiceTest {

    @Autowired
    private ReceptionistRepository receptionistRepository;

    private ReceptionistService receptionistService;

    @BeforeEach
    void setUp() {
        receptionistService = new ReceptionistService(receptionistRepository);
    }

    @Test
    void addReceptionistPersistsAndReturnsIt() {
        Receptionist saved = receptionistService.addReceptionist(new Receptionist("Rana Youssef", 27));

        assertEquals("Rana Youssef", saved.getName());
        assertTrue(receptionistService.findById(saved.getId()).isPresent());
    }


    @Test
    void getByIdOrThrowThrowsWhenMissing() {
        assertThrows(ReceptionistNotFoundException.class, () -> receptionistService.getByIdOrThrow(404));
    }

    @Test
    void getAllSortedByNameOrdersAlphabetically() {
        receptionistService.addReceptionist(new Receptionist("Zara Ahmed", 22));
        receptionistService.addReceptionist(new Receptionist("Amir Saleh", 45));

        List<Receptionist> sorted = receptionistService.getAllSortedByName();
        assertEquals("Amir Saleh", sorted.get(0).getName());
        assertEquals("Zara Ahmed", sorted.get(1).getName());
    }
}
