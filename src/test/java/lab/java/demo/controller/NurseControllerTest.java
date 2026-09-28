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

import lab.java.demo.dto.NurseRequest;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class NurseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private NurseRequest validRequest(String name, int age, String department) {
        NurseRequest request = new NurseRequest();
        request.setName(name);
        request.setAge(age);
        request.setDepartment(department);
        return request;
    }

    @Test
    void addingANurseWithoutCredentialsIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/nurses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("Nadia Cruz", 35, "ICU"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "NURSE")
    void onlyAdminCanAddANurse() throws Exception {
        mockMvc.perform(post("/api/nurses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("Nadia Cruz", 35, "ICU"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAddAndThenStaffCanReadANurse() throws Exception {
        String body = mockMvc.perform(post("/api/nurses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("Nadia Cruz", 35, "ICU"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.department").value("ICU"))
                .andReturn().getResponse().getContentAsString();

        int id = objectMapper.readTree(body).at("/data/id").asInt();

        mockMvc.perform(get("/api/nurses/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Nadia Cruz"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addingANurseWithABlankDepartmentIsRejected() throws Exception {
        mockMvc.perform(post("/api/nurses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("Nadia Cruz", 35, ""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void readingAnUnknownNurseReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/nurses/{id}", 999_999))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void patientRoleCannotReadTheNurseDirectory() throws Exception {
        // Issue 21: nurses are an internal staff directory, not
        // patient-facing.
        mockMvc.perform(get("/api/nurses/{id}", 1))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    void departmentAndSortedEndpointsAreReachableByStaff() throws Exception {
        mockMvc.perform(get("/api/nurses/department/{department}", "ICU"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/nurses/sorted/name"))
                .andExpect(status().isOk());
    }
}
