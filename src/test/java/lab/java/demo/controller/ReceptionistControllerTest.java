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

import lab.java.demo.dto.ReceptionistRequest;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ReceptionistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private ReceptionistRequest validRequest(String name, int age) {
        ReceptionistRequest request = new ReceptionistRequest();
        request.setName(name);
        request.setAge(age);
        return request;
    }

    @Test
    void addingAReceptionistWithoutCredentialsIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/receptionists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("Rana Youssef", 27))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    void onlyAdminCanAddAReceptionist() throws Exception {
        mockMvc.perform(post("/api/receptionists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("Rana Youssef", 27))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAddAndThenStaffCanReadAReceptionist() throws Exception {
        String body = mockMvc.perform(post("/api/receptionists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("Rana Youssef", 27))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Rana Youssef"))
                .andReturn().getResponse().getContentAsString();

        int id = objectMapper.readTree(body).at("/data/id").asInt();

        mockMvc.perform(get("/api/receptionists/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.age").value(27));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addingAReceptionistWithABlankNameIsRejected() throws Exception {
        mockMvc.perform(post("/api/receptionists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("", 27))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "NURSE")
    void readingAnUnknownReceptionistReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/receptionists/{id}", 999_999))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void patientRoleCannotReadTheReceptionistDirectory() throws Exception {
        mockMvc.perform(get("/api/receptionists/{id}", 1))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void sortedEndpointIsReachableByStaff() throws Exception {
        mockMvc.perform(get("/api/receptionists/sorted/name"))
                .andExpect(status().isOk());
    }
}
