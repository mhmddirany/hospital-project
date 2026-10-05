package lab.java.demo.dto;

import lab.java.demo.Models.User;

/**
 * Public shape of a User account: deliberately leaves out the password
 * hash, which should never reach a client.
 */
public class UserResponse {

    private final int id;
    private final String username;
    private final String role;

    public UserResponse(int id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole().name());
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
}
