package lab.java.demo.security;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lab.java.demo.Models.User;
import lab.java.demo.repository.UserRepository;

/**
 * Issue 21: replaces the old {@code InMemoryUserDetailsManager} bean in
 * SecurityConfig. Looks a username up in the real (in-memory-repository-
 * backed) UserRepository instead of a fixed, hard-coded account list, and
 * maps its Role onto a single Spring Security authority
 * ({@code ROLE_<name>}).
 *
 * <p>Deliberately throws the generic {@link UsernameNotFoundException}
 * rather than our own UserNotFoundException: Spring Security's
 * DaoAuthenticationProvider catches it and reports the same "bad
 * credentials" failure it would for a wrong password, so a login attempt
 * can't be used to enumerate which usernames exist.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No account for username '" + username + "'"));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
    }
}
