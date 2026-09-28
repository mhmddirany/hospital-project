package lab.java.demo.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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

import lab.java.demo.dto.DoctorRequest;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private DoctorRequest validRequest(String name, int age, String specialty) {
        DoctorRequest request = new DoctorRequest();
        request.setName(name);
        request.setAge(age);
        request.setSpecialty(specialty);
        return request;
    }

    @Test
    void addingADoctorWithoutCredentialsIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("Dr. Kim", 50, "Cardiology"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void onlyAdminCanAddADoctor() throws Exception {
        mockMvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("Dr. Kim", 50, "Cardiology"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAddAndThenAnyRoleCanReadADoctor() throws Exception {
        String body = mockMvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("Dr. Kim", 50, "Cardiology"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.specialty").value("Cardiology"))
                .andExpect(jsonPath("$.data.availability").value(true))
                .andReturn().getResponse().getContentAsString();

        int id = objectMapper.readTree(body).at("/data/id").asInt();

        // Any authenticated role -- including PATIENT -- can browse doctors;
        // that's needed just to pick one and book an appointment.
        mockMvc.perform(get("/api/doctors/{id}", id).with(user("patient-user").roles("PATIENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Dr. Kim"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addingADoctorWithABlankSpecialtyIsRejected() throws Exception {
        mockMvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("Dr. Kim", 50, ""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "NURSE")
    void readingAnUnknownDoctorReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/doctors/{id}", 999_999))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void availabilityEndpointReflectsTheDoctorsFlag() throws Exception {
        String body = mockMvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("Dr. Patel", 45, "Neurology"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        int id = objectMapper.readTree(body).at("/data/id").asInt();

        mockMvc.perform(get("/api/doctors/{id}/availability", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void specialtyAndSortedEndpointsAreReachableByAnyAuthenticatedRole() throws Exception {
        mockMvc.perform(get("/api/doctors/specialty/{specialty}", "Cardiology"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/doctors/sorted/name"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/doctors/sorted/specialty"))
                .andExpect(status().isOk());
    }
}
