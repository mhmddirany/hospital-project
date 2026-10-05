package lab.java.demo.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import lab.java.demo.Models.Role;

/**
 * Issue 21: issues and validates the JSON Web Tokens that replace HTTP
 * Basic for this API. A token's subject is the username and it carries
 * the user's Role as a custom claim, so a request can be authorized from
 * the token alone, without looking the user back up on every call (see
 * JwtAuthenticationFilter).
 *
 * <p>The signing key comes from configuration ({@code app.jwt.secret} in
 * application.properties, overridable with the {@code APP_JWT_SECRET}
 * environment variable) rather than a literal in source -- the default
 * shipped here is for local development only and must be overridden
 * before a real deployment (see README).
 */
@Component
public class JwtService {

    private static final String ROLE_CLAIM = "role";

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                       @Value("${app.jwt.expiration-ms:3600000}") long expirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(String username, Role role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(username)
                .claim(ROLE_CLAIM, role.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Returns the token's claims if it's well-formed, correctly signed,
     * and not expired -- null otherwise. Callers (JwtAuthenticationFilter)
     * treat a null return the same as "no credentials supplied" rather
     * than surfacing parser internals to the client.
     */
    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }

    public String extractUsername(Claims claims) {
        return claims.getSubject();
    }

    public String extractRole(Claims claims) {
        return claims.get(ROLE_CLAIM, String.class);
    }
}
