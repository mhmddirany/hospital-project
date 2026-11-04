package lab.java.demo.Models;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A login account (Issue 21), now stored in the {@code users} table.
 * The username has a database UNIQUE constraint, so two accounts can never
 * share one even under concurrent requests (UserService also checks first,
 * to give a clear 409 message in the normal case).
 *
 * <p>{@code passwordHash} is always a BCrypt hash -- never a plaintext
 * password. Hashing happens in {@link lab.java.demo.service.UserService}
 * before a User is ever constructed.
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * Database-generated primary key (step 1: replaces the old in-memory
     * AtomicInteger counter). {@code null} until the entity is first saved.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private Role role;

    /** Required by JPA; not for application code. */
    protected User() {
    }

    public User(String username, String passwordHash, Role role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public Integer getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    // ---------- equals / hashCode ----------
    // Identity is the database id. Two unsaved instances (id still null)
    // are only equal to themselves. hashCode is deliberately constant per
    // class so it doesn't change when the id is assigned on save -- the
    // standard pattern for JPA entities with generated ids.

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User other = (User) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
