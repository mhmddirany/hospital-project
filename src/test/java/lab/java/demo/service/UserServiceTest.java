package lab.java.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import lab.java.demo.Models.Role;
import lab.java.demo.Models.User;
import lab.java.demo.exception.UserConflictException;
import lab.java.demo.exception.UserNotFoundException;
import lab.java.demo.repository.UserRepository;

class UserServiceTest {

    private UserService userService;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(new UserRepository(), passwordEncoder);
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
