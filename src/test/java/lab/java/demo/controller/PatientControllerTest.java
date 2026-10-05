package lab.java.demo.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

import lab.java.demo.dto.PatientRequest;

/**
 * Issue 24: MockMvc integration test covering the real controller ->
 * service -> repository -> exception-handler stack, plus the Issue 21
 * authorization rules for this endpoint group (patient records are
 * clinical/front-desk-only; PATIENT itself is excluded -- see
 * SecurityConfig's class Javadoc for why).
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private PatientRequest validRequest(String name, int age) {
        PatientRequest request = new PatientRequest();
        request.setName(name);
        request.setAge(age);
        return request;
    }

    @Test
    void registeringWithoutCredentialsIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("Alex Rivera", 40))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void patientRoleCannotRegisterANewPatient() throws Exception {
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("Alex Rivera", 40))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    void receptionistCanRegisterAndThenReadBackAPatient() throws Exception {
        String body = mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("Alex Rivera", 40))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Alex Rivera"))
                .andExpect(jsonPath("$.data.age").value(40))
                .andReturn().getResponse().getContentAsString();

        int id = objectMapper.readTree(body).at("/data/id").asInt();

        mockMvc.perform(get("/api/patients/{id}", id).with(user("nurse-user").roles("NURSE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Alex Rivera"));
    }

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    void registeringWithContactInfoReturnsItBackInTheResponse() throws Exception {
        // Issue 16 follow-up: email/phone are optional, needed only so a
        // real notifier (SmtpEmailNotifier/TwilioSmsNotifier) has
        // somewhere to send a reminder -- this just confirms they round
        // trip through registration and the response DTO.
        PatientRequest request = validRequest("Jordan Lee", 35);
        request.setEmail("jordan.lee@example.com");
        request.setPhone("+1 555-123-4567");

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("jordan.lee@example.com"))
                .andExpect(jsonPath("$.data.phone").value("+1 555-123-4567"));
    }

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    void registeringWithoutContactInfoLeavesItNull() throws Exception {
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("Alex Rivera", 40))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").doesNotExist())
                .andExpect(jsonPath("$.data.phone").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    void registeringWithAMalformedEmailIsRejected() throws Exception {
        PatientRequest request = validRequest("Alex Rivera", 40);
        request.setEmail("not-an-email");

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    void registeringWithAMalformedPhoneIsRejected() throws Exception {
        PatientRequest request = validRequest("Alex Rivera", 40);
        request.setPhone("abc");

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    void registeringWithABlankNameIsRejected() throws Exception {
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("", 40))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    void registeringWithAnOutOfRangeAgeIsRejected() throws Exception {
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("Alex Rivera", 200))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void readingAnUnknownPatientReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/patients/{id}", 999_999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void patientRoleCannotReadPatientRecords() throws Exception {
        // Issue 21: no PATIENT account is linked to a specific Patient
        // entity, so letting PATIENT read this endpoint would let any
        // patient look up any other patient's data.
        mockMvc.perform(get("/api/patients/{id}", 1))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    void searchAndSortedEndpointsAreReachableByFrontDeskStaff() throws Exception {
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("Casey Morgan", 33))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/patients/search/{name}", "Casey"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Casey Morgan")));

        mockMvc.perform(get("/api/patients/sorted/name"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/patients/sorted/age"))
                .andExpect(status().isOk());
    }
}
