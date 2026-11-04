package lab.java.demo.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import lab.java.demo.Models.*;

/**
 * Step 1: a {@code @DataJpaTest} starts only the JPA layer (entities,
 * repositories, Flyway) against the test database -- no web layer, no
 * security. Each test runs in a transaction that is rolled back at the
 * end, so tests never see each other's data.
 *
 * <p>{@code replace = NONE} keeps the H2-in-PostgreSQL-mode database from
 * application-test.properties instead of Spring's default embedded one.
 * {@code flushAndClear()} forces a real INSERT and makes the next read go
 * back to the database, so these tests check what was actually stored,
 * not just objects sitting in Hibernate's cache.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReceptionistRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ReceptionistRepository repository;

    @Test
    void saveGeneratesAnIdAndTheRowCanBeReadBack() {
        Receptionist saved = repository.save(new Receptionist("Rana Youssef", 27));
        assertNotNull(saved.getId());
        flushAndClear();
        assertEquals("Rana Youssef", repository.findById(saved.getId()).orElseThrow().getName());
    }

    @Test
    void findAllReturnsEverySavedReceptionist() {
        repository.save(new Receptionist("Rana Youssef", 27));
        repository.save(new Receptionist("Karim Nasser", 31));
        flushAndClear();
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void findByIdReturnsEmptyWhenMissing() {
        assertTrue(repository.findById(404_404).isEmpty());
    }

    @Test
    void deleteByIdRemovesTheRow() {
        Integer id = repository.save(new Receptionist("Rana Youssef", 27)).getId();
        repository.deleteById(id);
        flushAndClear();
        assertFalse(repository.existsById(id));
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}
