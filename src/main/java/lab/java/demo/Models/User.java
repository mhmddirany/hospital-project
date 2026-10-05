package lab.java.demo.Models;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A login account (Issue 21). Replaces the hard-coded
 * {@code InMemoryUserDetailsManager} accounts that used to live directly
 * in {@code SecurityConfig}: an account is now a real entity, created at
 * runtime -- either through the admin-only {@code POST /api/users}
 * endpoint, or as the one bootstrap admin account seeded from
 * configuration at startup (see
 * {@link lab.java.demo.config.AdminBootstrap}) -- rather than a literal
 * baked into Java source.
 *
 * <p>Consistent with every other entity in this project (see Issues 6 and
 * 17), accounts live in an in-memory {@code ConcurrentHashMap}
 * ({@link lab.java.demo.repository.UserRepository}), not a real
 * database. A production deployment with real persistence requirements
 * would swap that repository for a JPA-backed one without needing to
 * change this class.
 *
 * <p>{@code passwordHash} is always a BCrypt hash -- never a plaintext
 * password. Hashing happens in {@link lab.java.demo.service.UserService}
 * before a User is ever constructed, using the same {@code
 * PasswordEncoder} bean the login flow verifies against.
 */
public class User {

    private static final AtomicInteger ID = new AtomicInteger(1);

    private final int id;
    private final String username;
    private String passwordHash;
    private Role role;

    public User(int id, String username, String passwordHash, Role role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public static int nextId() { return ID.getAndIncrement(); }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
