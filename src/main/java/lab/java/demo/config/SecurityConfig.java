package lab.java.demo.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lab.java.demo.dto.ErrorResponse;
import lab.java.demo.security.JwtAuthenticationFilter;

/**
 * Issue 21: role-based authorization for five roles (ADMIN, DOCTOR,
 * NURSE, RECEPTIONIST, PATIENT), same as before. What changed is how a
 * caller proves who they are.
 *
 * <p>Accounts used to live in a fixed, five-entry
 * {@code InMemoryUserDetailsManager} with passwords written directly
 * into this class, authenticated per-request with HTTP Basic. Per review
 * feedback, that's replaced with:
 * <ul>
 *   <li>a real {@code User} entity ({@link lab.java.demo.Models.User}),
 *       stored in {@link lab.java.demo.repository.UserRepository} -- an
 *       in-memory {@code ConcurrentHashMap}, consistent with every other
 *       repository in this project (see Issues 6 and 17), rather than
 *       reintroducing a JPA/H2 database just for this;</li>
 *   <li>a {@link lab.java.demo.security.CustomUserDetailsService} backed
 *       by that repository, replacing {@code InMemoryUserDetailsManager};</li>
 *   <li>BCrypt-hashed passwords (unchanged -- this app already hashed
 *       passwords, just for a hard-coded account list);</li>
 *   <li>user-management endpoints ({@code /api/users/**}) restricted to
 *       ADMIN, so accounts are created by an administrator instead of
 *       being baked into source;</li>
 *   <li>one bootstrap ADMIN account seeded at startup from configuration
 *       (see {@link AdminBootstrap}), solving the chicken-and-egg problem
 *       of needing an admin account to create the first admin account;</li>
 *   <li>stateless JWT authentication ({@code Authorization: Bearer
 *       <token>}, issued by {@code POST /api/auth/login}) in place of
 *       HTTP Basic, via
 *       {@link lab.java.demo.security.JwtAuthenticationFilter} -- more
 *       appropriate for a deployed frontend that shouldn't have to resend
 *       a password on every request.</li>
 * </ul>
 *
 * <p>There is deliberately no link between a PATIENT account and a
 * specific Patient entity -- no such account-to-entity relationship
 * exists anywhere in the domain model, and building one (patient
 * self-registration/login tied to a Patient id) is a separate feature,
 * not part of this issue. Because of that, PATIENT is intentionally the
 * most restricted role: it can browse doctors and request an
 * appointment, but it cannot list or read patient records or appointment
 * histories, since there is no way yet to tell "your own" record apart
 * from anyone else's.
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

    /**
     * Spring Security auto-wires a DaoAuthenticationProvider from the
     * CustomUserDetailsService and PasswordEncoder beans (see
     * InitializeUserDetailsBeanManagerConfigurer) as long as no
     * AuthenticationManager/AuthenticationProvider bean is defined
     * manually. This just exposes the resulting manager so AuthController
     * can call authenticate() directly.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ObjectMapper objectMapper,
                                            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
            // A stateless REST API authenticated per-request with a JWT
            // bearer token has no session cookie for CSRF to forge --
            // CSRF protection is designed for browser/cookie-based
            // sessions, which this API doesn't use.
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public: landing message, health check, error forwarding,
                // API docs, and logging in -- none of these expose patient
                // or appointment data.
                .requestMatchers("/", "/error", "/actuator/health").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers("/actuator/**").hasRole(ADMIN)

                // User accounts: admin-only, same as every other
                // onboarding endpoint below.
                .requestMatchers("/api/users/**").hasRole(ADMIN)

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
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(handling -> handling
                    .authenticationEntryPoint(jsonAuthenticationEntryPoint(objectMapper))
                    .accessDeniedHandler(jsonAccessDeniedHandler(objectMapper)));

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
