package lab.java.demo.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import lab.java.demo.Models.Role;
import lab.java.demo.Models.User;
import lab.java.demo.repository.UserRepository;

class CustomUserDetailsServiceTest {

    private UserRepository userRepository;
    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository();
        userDetailsService = new CustomUserDetailsService(userRepository);
    }

    @Test
    void loadsAKnownUserAndMapsItsRoleToASpringSecurityAuthority() {
        userRepository.save(new User(1, "dr.kim", "hashed-pw", Role.DOCTOR));

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
