package lab.java.demo.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import lab.java.demo.Models.Role;
import lab.java.demo.dto.UserCreateRequest;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UserCreateRequest validRequest(String username, String password, Role role) {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername(username);
        request.setPassword(password);
        request.setRole(role);
        return request;
    }

    @Test
    void creatingAUserWithoutCredentialsIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validRequest("dr.kim", "a-strong-password", Role.DOCTOR))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void onlyAdminCanCreateAUser() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validRequest("dr.kim", "a-strong-password", Role.DOCTOR))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "NURSE")
    void onlyAdminCanListUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanCreateThenListAndGetAUserWithoutThePasswordHashLeaking() throws Exception {
        String body = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validRequest("dr.patel", "a-strong-password", Role.DOCTOR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("dr.patel"))
                .andExpect(jsonPath("$.data.role").value("DOCTOR"))
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        int id = objectMapper.readTree(body).at("/data/id").asInt();

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("dr.patel"));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.username == 'dr.patel')]").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void creatingADuplicateUsernameReturnsConflict() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validRequest("dr.chen", "a-strong-password", Role.DOCTOR))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validRequest("dr.chen", "a-different-password", Role.NURSE))))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void creatingAUserWithATooShortPasswordIsRejected() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validRequest("dr.short", "1234567", Role.DOCTOR))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void creatingAUserWithAnUnknownRoleIsRejected() throws Exception {
        String malformedBody = "{\"username\":\"dr.bad\",\"password\":\"a-strong-password\",\"role\":\"SUPERADMIN\"}";

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void gettingAnUnknownUserReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 999_999))
                .andExpect(status().isNotFound());
    }
}
