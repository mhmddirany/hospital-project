package lab.java.demo.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Issue 21: reads the {@code Authorization: Bearer <token>} header on
 * each request and, if it carries a valid JWT, populates the
 * SecurityContext for that request -- the stateless replacement for HTTP
 * Basic's per-request credential check.
 *
 * <p>If the header is missing or the token doesn't validate, this filter
 * does nothing and lets the request continue unauthenticated; the
 * existing {@code authorizeHttpRequests} rules (and the custom JSON
 * AuthenticationEntryPoint in SecurityConfig) are what turn that into a
 * 401, exactly as they did when HTTP Basic credentials were missing or
 * wrong. It also never overwrites an authentication that's already
 * present on the context -- relevant for MockMvc tests using
 * {@code @WithMockUser}, which set one before the filter chain runs.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith(BEARER_PREFIX)
                || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length());
        Claims claims = jwtService.parseClaims(token);

        if (claims != null) {
            String username = jwtService.extractUsername(claims);
            String role = jwtService.extractRole(claims);

            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
