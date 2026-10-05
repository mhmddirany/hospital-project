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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import lab.java.demo.dto.LoginRequest;

/**
 * End-to-end coverage for Issue 21's login flow -- deliberately without
 * {@code @WithMockUser}, since the point here is to exercise the real
 * AuthenticationManager / CustomUserDetailsService / JwtService /
 * JwtAuthenticationFilter wiring, not a mocked SecurityContext.
 *
 * <p>Credentials below match the {@code app.admin.username} /
 * {@code app.admin.password} defaults in application.properties, which
 * AdminBootstrap seeds into a fresh ApplicationContext at startup -- if
 * those defaults ever change, this test's literals need to change with
 * them.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AuthControllerTest {

    private static final String BOOTSTRAP_ADMIN_USERNAME = "admin";
    private static final String BOOTSTRAP_ADMIN_PASSWORD = "admin123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String loginJson(String username, String password) throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return objectMapper.writeValueAsString(request);
    }

    @Test
    void loggingInWithTheBootstrapAdminCredentialsReturnsAToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(BOOTSTRAP_ADMIN_USERNAME, BOOTSTRAP_ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value(BOOTSTRAP_ADMIN_USERNAME))
                .andExpect(jsonPath("$.data.role").value("ADMIN"))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void loggingInWithTheWrongPasswordIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(BOOTSTRAP_ADMIN_USERNAME, "not-the-right-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void loggingInWithAnUnknownUsernameFailsWithTheSameMessageAsAWrongPassword() throws Exception {
        // Same status and message as the wrong-password case above --
        // the point is that a login attempt can't be used to tell "no
        // such account" apart from "wrong password" for one that exists.
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("no-such-user", "whatever")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void loggingInWithABlankUsernameIsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("", BOOTSTRAP_ADMIN_PASSWORD)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void aTokenFromLoginGrantsAccessToAnAdminOnlyEndpoint() throws Exception {
        String loginBody = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(BOOTSTRAP_ADMIN_USERNAME, BOOTSTRAP_ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(loginBody).at("/data/token").asText();

        // Real round trip through JwtAuthenticationFilter -- no
        // @WithMockUser here.
        mockMvc.perform(get("/api/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void anAdminOnlyEndpointWithoutATokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void anAdminOnlyEndpointWithAGarbledTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }
}
