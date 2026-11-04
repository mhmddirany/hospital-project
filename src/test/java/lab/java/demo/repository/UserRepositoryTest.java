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
class UserRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserRepository repository;

    @Test
    void savesAndFindsByUsername() {
        Integer id = repository.save(new User("dr.kim", "hashed-pw", Role.DOCTOR)).getId();
        flushAndClear();

        User byUsername = repository.findByUsername("dr.kim").orElseThrow();
        assertEquals(id, byUsername.getId());
        assertEquals("hashed-pw", byUsername.getPasswordHash());
        assertEquals(Role.DOCTOR, byUsername.getRole());
    }

    @Test
    void findByUsernameReturnsEmptyWhenMissing() {
        assertTrue(repository.findByUsername("nobody").isEmpty());
    }

    @Test
    void existsByUsernameReflectsWhetherTheAccountExists() {
        repository.save(new User("dr.kim", "hashed-pw", Role.DOCTOR));
        assertTrue(repository.existsByUsername("dr.kim"));
        assertFalse(repository.existsByUsername("nurse.jane"));
    }

    @Test
    void theDatabaseRejectsADuplicateUsername() {
        // The UNIQUE constraint on users.username, independent of any
        // check in UserService -- this is what protects against two
        // concurrent requests creating the same account.
        repository.saveAndFlush(new User("dr.kim", "hashed-pw", Role.DOCTOR));
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new User("dr.kim", "hashed-pw-2", Role.NURSE)));
    }

    @Test
    void deleteByIdRemovesTheAccount() {
        Integer id = repository.save(new User("dr.kim", "hashed-pw", Role.DOCTOR)).getId();
        repository.deleteById(id);
        flushAndClear();
        assertTrue(repository.findByUsername("dr.kim").isEmpty());
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}
