package lab.java.demo.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import lab.java.demo.Models.NotificationChannel;
import lab.java.demo.dto.AppointmentRequest;
import lab.java.demo.dto.DoctorRequest;
import lab.java.demo.dto.PatientRequest;

/**
 * Issue 24, closing the loop on Issue 2: "AppointmentControllerTest" was
 * originally a second, dead-code implementation of appointment creation
 * living in src/main/java (removed as part of Issue 2/18). This is what
 * that name should always have meant -- a real test, in src/test/java,
 * for the one real AppointmentController.
 *
 * <p>Covers the full controller -> service -> repository -> exception
 * handler stack, plus the Issue 21 authorization rules for this endpoint
 * group.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private int registerPatient(String name, int age) throws Exception {
        PatientRequest request = new PatientRequest();
        request.setName(name);
        request.setAge(age);

        ResultActions result = mockMvc.perform(post("/api/patients")
                .with(user("receptionist-user").roles("RECEPTIONIST"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
        String body = result.andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).at("/data/id").asInt();
    }

    private int registerDoctor(String name, int age, String specialty) throws Exception {
        DoctorRequest request = new DoctorRequest();
        request.setName(name);
        request.setAge(age);
        request.setSpecialty(specialty);

        ResultActions result = mockMvc.perform(post("/api/doctors")
                .with(user("admin-user").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
        String body = result.andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).at("/data/id").asInt();
    }

    private String appointmentRequestJson(int patientId, int doctorId, LocalDateTime dateTime) throws Exception {
        AppointmentRequest request = new AppointmentRequest();
        request.setPatientId(patientId);
        request.setDoctorId(doctorId);
        request.setDateTime(dateTime);
        return objectMapper.writeValueAsString(request);
    }

    @Test
    void requestingAnAppointmentWithoutCredentialsIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentRequestJson(1, 1, LocalDateTime.now().plusDays(1))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void doctorRoleCannotRequestAnAppointmentOnItsOwnBehalf() throws Exception {
        int patientId = registerPatient("Alex Rivera", 40);
        int doctorId = registerDoctor("Dr. Kim", 50, "Cardiology");

        mockMvc.perform(post("/api/appointments")
                        .with(user("doctor-user").roles("DOCTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentRequestJson(patientId, doctorId, LocalDateTime.now().plusDays(1))))
                .andExpect(status().isForbidden());
    }

    @Test
    void patientCanRequestAnAppointmentAndDoctorCanConfirmIt() throws Exception {
        int patientId = registerPatient("Alex Rivera", 40);
        int doctorId = registerDoctor("Dr. Kim", 50, "Cardiology");

        String body = mockMvc.perform(post("/api/appointments")
                        .with(user("patient-user").roles("PATIENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentRequestJson(patientId, doctorId, LocalDateTime.now().plusDays(1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REQUESTED"))
                .andReturn().getResponse().getContentAsString();

        int appointmentId = objectMapper.readTree(body).at("/data/id").asInt();

        mockMvc.perform(put("/api/appointments/{id}/confirm", appointmentId)
                        .with(user("doctor-user").roles("DOCTOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }

    @Test
    void patientRoleCannotConfirmAnAppointment() throws Exception {
        int patientId = registerPatient("Alex Rivera", 40);
        int doctorId = registerDoctor("Dr. Kim", 50, "Cardiology");

        String body = mockMvc.perform(post("/api/appointments")
                        .with(user("patient-user").roles("PATIENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentRequestJson(patientId, doctorId, LocalDateTime.now().plusDays(1))))
                .andReturn().getResponse().getContentAsString();
        int appointmentId = objectMapper.readTree(body).at("/data/id").asInt();

        mockMvc.perform(put("/api/appointments/{id}/confirm", appointmentId)
                        .with(user("patient-user").roles("PATIENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void requestingAnAppointmentWithAPastDateTimeIsRejected() throws Exception {
        int patientId = registerPatient("Alex Rivera", 40);
        int doctorId = registerDoctor("Dr. Kim", 50, "Cardiology");

        mockMvc.perform(post("/api/appointments")
                        .with(user("receptionist-user").roles("RECEPTIONIST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentRequestJson(patientId, doctorId, LocalDateTime.now().minusDays(1))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requestingAnAppointmentForAnUnknownPatientReturnsNotFound() throws Exception {
        int doctorId = registerDoctor("Dr. Kim", 50, "Cardiology");

        mockMvc.perform(post("/api/appointments")
                        .with(user("receptionist-user").roles("RECEPTIONIST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentRequestJson(999_999, doctorId, LocalDateTime.now().plusDays(1))))
                .andExpect(status().isNotFound());
    }

    @Test
    void requestingAnAppointmentForAnUnknownDoctorReturnsNotFound() throws Exception {
        int patientId = registerPatient("Alex Rivera", 40);

        mockMvc.perform(post("/api/appointments")
                        .with(user("receptionist-user").roles("RECEPTIONIST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentRequestJson(patientId, 999_999, LocalDateTime.now().plusDays(1))))
                .andExpect(status().isNotFound());
    }

    @Test
    void doubleBookingTheSameDoctorAtTheSameTimeReturnsConflict() throws Exception {
        int patientId = registerPatient("Alex Rivera", 40);
        int doctorId = registerDoctor("Dr. Kim", 50, "Cardiology");
        LocalDateTime slot = LocalDateTime.now().plusDays(1);

        mockMvc.perform(post("/api/appointments")
                        .with(user("receptionist-user").roles("RECEPTIONIST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentRequestJson(patientId, doctorId, slot)))
                .andExpect(status().isOk());

        int otherPatientId = registerPatient("Blair Chen", 30);
        mockMvc.perform(post("/api/appointments")
                        .with(user("receptionist-user").roles("RECEPTIONIST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentRequestJson(otherPatientId, doctorId, slot)))
                .andExpect(status().isConflict());
    }

    @Test
    void confirmingAnUnknownAppointmentReturnsNotFound() throws Exception {
        mockMvc.perform(put("/api/appointments/{id}/confirm", 999_999)
                        .with(user("doctor-user").roles("DOCTOR")))
                .andExpect(status().isNotFound());
    }

    @Test
    void receptionistCanCancelAnAppointment() throws Exception {
        int patientId = registerPatient("Alex Rivera", 40);
        int doctorId = registerDoctor("Dr. Kim", 50, "Cardiology");

        String body = mockMvc.perform(post("/api/appointments")
                        .with(user("receptionist-user").roles("RECEPTIONIST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentRequestJson(patientId, doctorId, LocalDateTime.now().plusDays(1))))
                .andReturn().getResponse().getContentAsString();
        int appointmentId = objectMapper.readTree(body).at("/data/id").asInt();

        mockMvc.perform(put("/api/appointments/{id}/cancel", appointmentId)
                        .with(user("receptionist-user").roles("RECEPTIONIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELED"));
    }

    @Test
    void listingAppointmentsForAPatientIsStaffOnlyNotPatientRole() throws Exception {
        int patientId = registerPatient("Alex Rivera", 40);
        int doctorId = registerDoctor("Dr. Kim", 50, "Cardiology");
        mockMvc.perform(post("/api/appointments")
                .with(user("receptionist-user").roles("RECEPTIONIST"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(appointmentRequestJson(patientId, doctorId, LocalDateTime.now().plusDays(1))));

        mockMvc.perform(get("/api/appointments/patient/{patientId}", patientId)
                        .with(user("nurse-user").roles("NURSE")))
                .andExpect(status().isOk());

        // Issue 21: PATIENT is excluded -- no account-to-record link exists
        // yet, so it can't be scoped to "this patient's own appointments".
        mockMvc.perform(get("/api/appointments/patient/{patientId}", patientId)
                        .with(user("patient-user").roles("PATIENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void listingAppointmentsForADoctorIsReachableByClinicalStaff() throws Exception {
        int patientId = registerPatient("Alex Rivera", 40);
        int doctorId = registerDoctor("Dr. Kim", 50, "Cardiology");
        mockMvc.perform(post("/api/appointments")
                .with(user("receptionist-user").roles("RECEPTIONIST"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(appointmentRequestJson(patientId, doctorId, LocalDateTime.now().plusDays(1))));

        mockMvc.perform(get("/api/appointments/doctor/{doctorId}", doctorId)
                        .with(user("doctor-user").roles("DOCTOR")))
                .andExpect(status().isOk());
    }

    @Test
    void onlyAdminCanTriggerReminders() throws Exception {
        mockMvc.perform(post("/api/appointments/reminders/tomorrow")
                        .with(user("doctor-user").roles("DOCTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(NotificationChannel.EMAIL))))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminTriggeringRemindersWithEmptyChannelsIsRejected() throws Exception {
        mockMvc.perform(post("/api/appointments/reminders/tomorrow")
                        .with(user("admin-user").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminTriggeringRemindersWithValidChannelsSucceeds() throws Exception {
        mockMvc.perform(post("/api/appointments/reminders/tomorrow")
                        .with(user("admin-user").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(NotificationChannel.EMAIL, NotificationChannel.SMS))))
                .andExpect(status().isOk());
    }
}
