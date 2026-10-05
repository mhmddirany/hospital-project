package lab.java.demo.Models;

/**
 * The application's five user roles (Issue 21). A Role maps 1:1 onto a
 * Spring Security authority of the form {@code ROLE_<name>} -- see
 * {@link lab.java.demo.security.CustomUserDetailsService} and
 * {@link lab.java.demo.security.JwtAuthenticationFilter}.
 */
public enum Role {
    ADMIN,
    DOCTOR,
    NURSE,
    RECEPTIONIST,
    PATIENT
}
