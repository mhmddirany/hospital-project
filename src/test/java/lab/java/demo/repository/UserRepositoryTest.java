package lab.java.demo.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lab.java.demo.Models.Role;
import lab.java.demo.Models.User;
import lab.java.demo.exception.UserConflictException;

class UserRepositoryTest {

    private UserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new UserRepository();
    }

    @Test
    void savesAndFindsByIdAndByUsername() {
        User saved = repository.save(new User(1, "dr.kim", "hashed-pw", Role.DOCTOR));

        Optional<User> byId = repository.findById(1);
        assertTrue(byId.isPresent());
        assertEquals(saved, byId.get());

        Optional<User> byUsername = repository.findByUsername("dr.kim");
        assertTrue(byUsername.isPresent());
        assertEquals(Role.DOCTOR, byUsername.get().getRole());
    }

    @Test
    void findByIdReturnsEmptyWhenMissing() {
        assertTrue(repository.findById(404).isEmpty());
    }

    @Test
    void findByUsernameReturnsEmptyWhenMissing() {
        assertTrue(repository.findByUsername("nobody").isEmpty());
    }

    @Test
    void savingADuplicateIdThrowsConflict() {
        repository.save(new User(1, "dr.kim", "hashed-pw", Role.DOCTOR));

        assertThrows(UserConflictException.class,
                () -> repository.save(new User(1, "someone.else", "hashed-pw-2", Role.NURSE)));

        // The original account is untouched.
        assertEquals("dr.kim", repository.findById(1).orElseThrow().getUsername());
    }

    @Test
    void savingADuplicateUsernameThrowsConflictAndRollsBackTheIdIndex() {
        repository.save(new User(1, "dr.kim", "hashed-pw", Role.DOCTOR));

        assertThrows(UserConflictException.class,
                () -> repository.save(new User(2, "dr.kim", "hashed-pw-2", Role.NURSE)));

        // The id-keyed insert for the rejected save must have been rolled
        // back -- id 2 should not exist at all.
        assertTrue(repository.findById(2).isEmpty());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void deleteByIdRemovesBothIndexesAndReportsWhetherAnythingWasRemoved() {
        repository.save(new User(1, "dr.kim", "hashed-pw", Role.DOCTOR));

        assertTrue(repository.deleteById(1));
        assertFalse(repository.deleteById(1));
        assertTrue(repository.findByUsername("dr.kim").isEmpty());
    }

    @Test
    void findAllReturnsEveryUser() {
        repository.save(new User(1, "dr.kim", "hashed-pw", Role.DOCTOR));
        repository.save(new User(2, "nurse.jane", "hashed-pw-2", Role.NURSE));

        assertEquals(2, repository.findAll().size());
    }
}
