package lab.java.demo.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lab.java.demo.dto.ErrorResponse;

/**
 * Issue 21: the application had no Spring Security dependency and no
 * authorization rules at all -- every endpoint, including patient
 * demographics and appointment data, was reachable by anyone who could
 * reach the port. This wires up HTTP Basic authentication plus role-based
 * authorization for five roles: ADMIN, DOCTOR, NURSE, RECEPTIONIST, and
 * PATIENT.
 *
 * <p>Accounts live in an {@link InMemoryUserDetailsManager} rather than a
 * real user store, consistent with the rest of this project: every
 * repository here is an in-memory {@code ConcurrentHashMap}, not a database
 * (see Issues 6 and 17). A production deployment would replace this with a
 * {@link UserDetailsService} backed by a persistent, hashed-password user
 * table and a real registration/admin-provisioning flow -- not something to
 * fake here.
 *
 * <p>Demo accounts (username / password / role), for exercising the API:
 * <pre>
 *   admin        / admin123        / ADMIN
 *   doctor       / doctor123       / DOCTOR
 *   nurse        / nurse123        / NURSE
 *   receptionist / receptionist123 / RECEPTIONIST
 *   patient      / patient123      / PATIENT
 * </pre>
 *
 * <p>There is deliberately no link between a PATIENT account and a specific
 * Patient entity -- no such account-to-entity relationship exists anywhere
 * in the domain model, and building one (patient self-registration/login
 * tied to a Patient id) is a separate feature, not part of this issue.
 * Because of that, PATIENT is intentionally the most restricted role: it
 * can browse doctors and request an appointment, but it cannot list or
 * read patient records or appointment histories, since there is no way yet
 * to tell "your own" record apart from anyone else's.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String DOCTOR = "DOCTOR";
    private static final String NURSE = "NURSE";
    private static final String RECEPTIONIST = "RECEPTIONIST";
    private static final String PATIENT = "PATIENT";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(User.withUsername("admin")
                .password(encoder.encode("admin123"))
                .roles(ADMIN)
                .build());
        manager.createUser(User.withUsername("doctor")
                .password(encoder.encode("doctor123"))
                .roles(DOCTOR)
                .build());
        manager.createUser(User.withUsername("nurse")
                .password(encoder.encode("nurse123"))
                .roles(NURSE)
                .build());
        manager.createUser(User.withUsername("receptionist")
                .password(encoder.encode("receptionist123"))
                .roles(RECEPTIONIST)
                .build());
        manager.createUser(User.withUsername("patient")
                .password(encoder.encode("patient123"))
                .roles(PATIENT)
                .build());
        return manager;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
        http
            // A stateless REST API authenticated per-request with HTTP Basic
            // has no session cookie for CSRF to forge -- CSRF protection is
            // designed for browser/cookie-based sessions, which this API
            // doesn't use.
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public: landing message, health check, error forwarding,
                // and API docs -- none of these expose patient or
                // appointment data.
                .requestMatchers("/", "/error", "/actuator/health").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/**").hasRole(ADMIN)

                // Doctors: anyone logged in can browse/search doctors and
                // check availability -- that's needed just to book an
                // appointment. Only ADMIN onboards a new doctor.
                .requestMatchers(HttpMethod.POST, "/api/doctors").hasRole(ADMIN)
                .requestMatchers(HttpMethod.GET, "/api/doctors/**")
                        .hasAnyRole(ADMIN, DOCTOR, NURSE, RECEPTIONIST, PATIENT)

                // Nurses/receptionists: an internal staff directory, not
                // patient-facing.
                .requestMatchers(HttpMethod.POST, "/api/nurses").hasRole(ADMIN)
                .requestMatchers(HttpMethod.GET, "/api/nurses/**")
                        .hasAnyRole(ADMIN, DOCTOR, NURSE, RECEPTIONIST)
                .requestMatchers(HttpMethod.POST, "/api/receptionists").hasRole(ADMIN)
                .requestMatchers(HttpMethod.GET, "/api/receptionists/**")
                        .hasAnyRole(ADMIN, DOCTOR, NURSE, RECEPTIONIST)

                // Patients: registering a new patient is front-desk/admin
                // work. Reading patient records is clinical/front-desk-only;
                // PATIENT is excluded here because there is no
                // account-to-record link yet (see class Javadoc), so
                // letting PATIENT hit this endpoint would let any patient
                // look up any other patient's name and age by id or by
                // name search.
                .requestMatchers(HttpMethod.POST, "/api/patients").hasAnyRole(ADMIN, RECEPTIONIST)
                .requestMatchers(HttpMethod.GET, "/api/patients/**")
                        .hasAnyRole(ADMIN, DOCTOR, NURSE, RECEPTIONIST)

                // Appointments: a patient can request their own appointment;
                // confirming/cancelling is a clinical or front-desk
                // decision, not something the requesting patient does
                // themselves. Listing appointments by patient/doctor id is
                // excluded for PATIENT for the same reason patient records
                // are above.
                .requestMatchers(HttpMethod.POST, "/api/appointments/reminders/tomorrow").hasRole(ADMIN)
                .requestMatchers(HttpMethod.POST, "/api/appointments")
                        .hasAnyRole(ADMIN, RECEPTIONIST, PATIENT)
                .requestMatchers(HttpMethod.PUT, "/api/appointments/*/confirm", "/api/appointments/*/cancel")
                        .hasAnyRole(ADMIN, DOCTOR, RECEPTIONIST)
                .requestMatchers(HttpMethod.GET, "/api/appointments/**")
                        .hasAnyRole(ADMIN, DOCTOR, NURSE, RECEPTIONIST)

                // Anything else: safe default is to require a logged-in
                // user rather than silently allowing it through.
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> basic.authenticationEntryPoint(jsonAuthenticationEntryPoint(objectMapper)))
            .exceptionHandling(handling -> handling.accessDeniedHandler(jsonAccessDeniedHandler(objectMapper)));

        return http.build();
    }

    // Spring Security's default 401/403 responses are plain text (or empty)
    // and don't match the rest of this API's ErrorResponse JSON shape (see
    // GlobalExceptionHandler). These two handlers keep auth failures
    // consistent with every other error response the API returns.

    private AuthenticationEntryPoint jsonAuthenticationEntryPoint(ObjectMapper objectMapper) {
        return (request, response, authException) -> writeError(
                response, objectMapper, HttpStatus.UNAUTHORIZED, request,
                "Authentication is required to access this resource");
    }

    private AccessDeniedHandler jsonAccessDeniedHandler(ObjectMapper objectMapper) {
        return (request, response, accessDeniedException) -> writeError(
                response, objectMapper, HttpStatus.FORBIDDEN, request,
                "You do not have permission to access this resource");
    }

    private void writeError(HttpServletResponse response, ObjectMapper objectMapper, HttpStatus status,
                             HttpServletRequest request, String message) throws IOException {
        ErrorResponse body = new ErrorResponse(status.value(), status.getReasonPhrase(), message,
                request.getRequestURI());
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
