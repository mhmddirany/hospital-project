package lab.java.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lab.java.demo.Models.Role;
import lab.java.demo.Models.User;
import lab.java.demo.exception.UserConflictException;
import lab.java.demo.exception.UserNotFoundException;
import lab.java.demo.repository.UserRepository;

/**
 * Issue 21: user accounts, now created through this service instead of
 * being hard-coded in SecurityConfig. A password never reaches the
 * repository in plaintext -- it's hashed here with the same BCrypt
 * PasswordEncoder bean the login flow verifies against.
 */
@Service
public class UserService extends BaseCrudService<User, Integer> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // -------- BaseCrudService hooks --------

    @Override
    protected List<User> rawFindAll() {
        return userRepository.findAll();
    }

    @Override
    protected Optional<User> rawFindById(Integer id) {
        return userRepository.findById(id);
    }

    @Override
    protected User rawSave(User entity) {
        return userRepository.save(entity);
    }

    @Override
    protected boolean rawDeleteById(Integer id) {
        return userRepository.deleteById(id);
    }

    // -------- User-specific API --------

    @Override
    public Optional<User> findById(Integer id) {
        return super.findById(id);
    }

    /**
     * Hashes {@code rawPassword} and persists a new account. Checked here
     * first (rather than relying solely on the repository's own
     * putIfAbsent guard) so a duplicate username comes back as a clear,
     * specific message instead of whatever the lower-level conflict check
     * happens to say.
     */
    public User createUser(String username, String rawPassword, Role role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new UserConflictException("Username '" + username + "' is already taken");
        }
        User user = new User(User.nextId(), username, passwordEncoder.encode(rawPassword), role);
        return save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User getByIdOrThrow(int id) {
        return findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }
}
