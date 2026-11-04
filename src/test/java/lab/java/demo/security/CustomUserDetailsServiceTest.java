package lab.java.demo.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import lab.java.demo.Models.Role;
import lab.java.demo.Models.User;
import lab.java.demo.repository.UserRepository;

/**
 * Step 1: runs against the real JPA repositories and the test database
 * ({@code @DataJpaTest}), with the service itself still constructed by
 * hand -- the test stays a focused service test, but every save and query
 * now goes through real SQL. Each test is rolled back at the end.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomUserDetailsServiceTest {

    @Autowired
    private UserRepository userRepository;
    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new CustomUserDetailsService(userRepository);
    }

    @Test
    void loadsAKnownUserAndMapsItsRoleToASpringSecurityAuthority() {
        userRepository.save(new User("dr.kim", "hashed-pw", Role.DOCTOR));

        UserDetails details = userDetailsService.loadUserByUsername("dr.kim");

        assertEquals("dr.kim", details.getUsername());
        assertEquals("hashed-pw", details.getPassword());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR")));
    }

    @Test
    void throwsUsernameNotFoundExceptionForAnUnknownUsername() {
        // Deliberately the generic Spring Security exception, not our own
        // UserNotFoundException -- so a login attempt can't be used to
        // tell a wrong username apart from a wrong password (see
        // AuthController / GlobalExceptionHandler).
        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("nobody"));
    }
}
