package lab.java.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import lab.java.demo.Models.Role;
import lab.java.demo.Models.User;
import lab.java.demo.exception.UserConflictException;
import lab.java.demo.exception.UserNotFoundException;
import lab.java.demo.repository.UserRepository;

/**
 * Step 1: runs against the real JPA repositories and the test database
 * ({@code @DataJpaTest}), with the service itself still constructed by
 * hand -- the test stays a focused service test, but every save and query
 * now goes through real SQL. Each test is rolled back at the end.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    private UserService userService;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void createUserHashesThePasswordRatherThanStoringItInPlaintext() {
        User saved = userService.createUser("dr.kim", "s3cret-password", Role.DOCTOR);

        assertNotEquals("s3cret-password", saved.getPasswordHash());
        assertTrue(passwordEncoder.matches("s3cret-password", saved.getPasswordHash()));
        assertEquals(Role.DOCTOR, saved.getRole());
    }

    @Test
    void creatingADuplicateUsernamePropagatesTheConflict() {
        userService.createUser("dr.kim", "s3cret-password", Role.DOCTOR);

        assertThrows(UserConflictException.class,
                () -> userService.createUser("dr.kim", "another-password", Role.NURSE));
    }

    @Test
    void getByIdOrThrowThrowsWhenMissing() {
        assertThrows(UserNotFoundException.class, () -> userService.getByIdOrThrow(404));
    }

    @Test
    void getByIdOrThrowReturnsTheCreatedUser() {
        User saved = userService.createUser("dr.kim", "s3cret-password", Role.DOCTOR);

        User found = userService.getByIdOrThrow(saved.getId());
        assertEquals("dr.kim", found.getUsername());
    }

    @Test
    void findByUsernameReturnsEmptyWhenMissing() {
        assertTrue(userService.findByUsername("nobody").isEmpty());
    }
}
