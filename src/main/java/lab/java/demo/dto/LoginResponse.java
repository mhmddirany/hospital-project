package lab.java.demo.dto;

/**
 * Returned on a successful login: the JWT to send as
 * {@code Authorization: Bearer <token>} on subsequent requests, plus the
 * identity it was issued for (handy for a frontend to show "logged in
 * as ..." without decoding the token itself).
 */
public class LoginResponse {

    private final String token;
    private final String username;
    private final String role;

    public LoginResponse(String token, String username, String role) {
        this.token = token;
        this.username = username;
        this.role = role;
    }

    public String getToken() { return token; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
}
