package lab.java.demo.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;

import lab.java.demo.Models.Role;

class JwtServiceTest {

    // 32+ chars, same minimum length requirement as app.jwt.secret in
    // application.properties -- see JwtService's Javadoc.
    private static final String TEST_SECRET = "test-only-jwt-signing-secret-at-least-32-bytes-long";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, 60_000L);
    }

    @Test
    void generatesATokenThatParsesBackToTheSameUsernameAndRole() {
        String token = jwtService.generateToken("dr.kim", Role.DOCTOR);

        Claims claims = jwtService.parseClaims(token);
        assertNotNull(claims);
        assertEquals("dr.kim", jwtService.extractUsername(claims));
        assertEquals("DOCTOR", jwtService.extractRole(claims));
    }

    @Test
    void rejectsATokenSignedWithADifferentSecret() {
        JwtService otherService = new JwtService("a-completely-different-32-byte-plus-secret-value", 60_000L);
        String token = otherService.generateToken("dr.kim", Role.DOCTOR);

        assertNull(jwtService.parseClaims(token));
    }

    @Test
    void rejectsAnExpiredToken() {
        // A negative expiration puts "expiry" in the past the instant the
        // token is issued, so this is deterministic -- no need to sleep.
        JwtService alreadyExpired = new JwtService(TEST_SECRET, -1_000L);
        String token = alreadyExpired.generateToken("dr.kim", Role.DOCTOR);

        assertNull(jwtService.parseClaims(token));
    }

    @Test
    void rejectsAMalformedToken() {
        assertNull(jwtService.parseClaims("not-a-real-jwt"));
    }
}
