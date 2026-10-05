package lab.java.demo.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lab.java.demo.Models.User;
import lab.java.demo.dto.ApiResponse;
import lab.java.demo.dto.UserCreateRequest;
import lab.java.demo.dto.UserResponse;
import lab.java.demo.service.UserService;

/**
 * Issue 21: user-account management, restricted to ADMIN by the
 * {@code /api/users/**} rule in SecurityConfig -- the same URL-based
 * authorization pattern every other admin-only route in this API uses
 * (no method-level {@code @PreAuthorize} needed). This is how new
 * accounts get created now that there's no hard-coded account list: an
 * admin logs in, then provisions one account per person who needs API
 * access.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        User user = userService.createUser(request.getUsername(), request.getPassword(), request.getRole());
        return new ApiResponse<>("User created successfully", UserResponse.from(user));
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> listUsers() {
        List<UserResponse> result = userService.findAll().stream()
                .map(UserResponse::from)
                .toList();
        return new ApiResponse<>("All users", result);
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getById(@PathVariable int id) {
        User user = userService.getByIdOrThrow(id);
        return new ApiResponse<>("User found", UserResponse.from(user));
    }
}
