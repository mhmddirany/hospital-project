package lab.java.demo.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import lab.java.demo.Models.Appointment;
import lab.java.demo.Models.AppointmentStatus;
import lab.java.demo.Models.Doctor;
import lab.java.demo.Models.Patient;

class TwilioSmsNotifierTest {

    // Test-only account values: Twilio.init(...) just caches these in
    // static fields (no network call -- see TwilioSmsNotifier's
    // Javadoc), so there's nothing real needed here. The actual
    // Message.creator(...).create() REST call is overridden below
    // instead of ever being exercised.
    private static final String TEST_SID = "ACtest0000000000000000000000000000";
    private static final String TEST_TOKEN = "test-auth-token";
    private static final String TEST_FROM_NUMBER = "+15551234567";

    // Records what would have been sent instead of actually calling
    // Twilio's REST API -- the same "override the one network-touching
    // seam" pattern used for the parts of this codebase that talk to the
    // outside world.
    private static class RecordingTwilioSmsNotifier extends TwilioSmsNotifier {
        String lastTo;
        String lastBody;

        RecordingTwilioSmsNotifier() {
            super(TEST_SID, TEST_TOKEN, TEST_FROM_NUMBER);
        }

        @Override
        protected void sendSms(String to, String body) {
            this.lastTo = to;
            this.lastBody = body;
        }
    }

    private Appointment appointmentFor(Patient patient) {
        Doctor doctor = new Doctor(1, "Dr. Kim", 50, "Cardiology");
        return new Appointment(1, LocalDateTime.now().plusDays(1), AppointmentStatus.CONFIRMED, patient, doctor);
    }

    @Test
    void sendsAnSmsToThePatientsPhoneNumberWhenOneIsOnFile() {
        Patient patient = new Patient(1, "Alex Rivera", 40);
        patient.setPhone("+15559876543");

        RecordingTwilioSmsNotifier notifier = new RecordingTwilioSmsNotifier();
        notifier.send(appointmentFor(patient));

        assertEquals("+15559876543", notifier.lastTo);
        assertTrue(notifier.lastBody.contains("Dr. Kim"));
    }

    @Test
    void skipsSendingWhenThePatientHasNoPhoneNumberOnFile() {
        Patient patient = new Patient(1, "Alex Rivera", 40);
        // phone left null

        RecordingTwilioSmsNotifier notifier = new RecordingTwilioSmsNotifier();
        notifier.send(appointmentFor(patient));

        assertNull(notifier.lastTo);
    }

    @Test
    void skipsSendingWhenThePatientsPhoneNumberIsBlank() {
        Patient patient = new Patient(1, "Alex Rivera", 40);
        patient.setPhone("   ");

        RecordingTwilioSmsNotifier notifier = new RecordingTwilioSmsNotifier();
        notifier.send(appointmentFor(patient));

        assertNull(notifier.lastTo);
    }
}
