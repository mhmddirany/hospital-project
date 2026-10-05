package lab.java.demo.controller;

import jakarta.validation.Valid;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lab.java.demo.Models.Role;
import lab.java.demo.dto.ApiResponse;
import lab.java.demo.dto.LoginRequest;
import lab.java.demo.dto.LoginResponse;
import lab.java.demo.security.JwtService;

/**
 * Issue 21: the one unauthenticated endpoint that issues credentials
 * (see the {@code permitAll()} rule for it in SecurityConfig). Delegates
 * the actual username/password check to the AuthenticationManager --
 * wired to CustomUserDetailsService and the BCrypt PasswordEncoder bean
 * in SecurityConfig -- instead of re-implementing it here, then hands
 * back a signed JWT. See JwtService and JwtAuthenticationFilter for how
 * that token is validated on later requests.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // Throws an AuthenticationException (unknown username or wrong
        // password -- deliberately indistinguishable, see
        // CustomUserDetailsService) for GlobalExceptionHandler to turn
        // into a 401.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        String roleName = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.replaceFirst("^ROLE_", ""))
                .orElseThrow();

        String token = jwtService.generateToken(authentication.getName(), Role.valueOf(roleName));

        return new ApiResponse<>("Login successful", new LoginResponse(token, authentication.getName(), roleName));
    }
}
