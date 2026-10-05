package lab.java.demo.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.InputStream;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import jakarta.mail.internet.MimeMessage;

import lab.java.demo.Models.Appointment;
import lab.java.demo.Models.AppointmentStatus;
import lab.java.demo.Models.Doctor;
import lab.java.demo.Models.Patient;

class SmtpEmailNotifierTest {

    private static final String FROM_ADDRESS = "no-reply@example.com";

    // Hand-rolled JavaMailSender test double, in keeping with this
    // project's existing style of real/recording objects rather than a
    // mocking framework (see AppointmentServiceTest's RecordingNotifier).
    // Only send(SimpleMailMessage) is ever actually used by
    // SmtpEmailNotifier; the rest of this wide interface just isn't
    // exercised here.
    private static class RecordingMailSender implements JavaMailSender {
        SimpleMailMessage lastMessage;

        @Override
        public void send(SimpleMailMessage simpleMessage) throws MailException {
            this.lastMessage = simpleMessage;
        }

        @Override
        public void send(SimpleMailMessage... simpleMessages) throws MailException {
            throw new UnsupportedOperationException("not used by this test");
        }

        @Override
        public MimeMessage createMimeMessage() {
            throw new UnsupportedOperationException("not used by this test");
        }

        @Override
        public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
            throw new UnsupportedOperationException("not used by this test");
        }

        @Override
        public void send(MimeMessage mimeMessage) throws MailException {
            throw new UnsupportedOperationException("not used by this test");
        }

        @Override
        public void send(MimeMessage... mimeMessages) throws MailException {
            throw new UnsupportedOperationException("not used by this test");
        }

        @Override
        public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
            throw new UnsupportedOperationException("not used by this test");
        }

        @Override
        public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException {
            throw new UnsupportedOperationException("not used by this test");
        }
    }

    private Appointment appointmentFor(Patient patient) {
        Doctor doctor = new Doctor(1, "Dr. Kim", 50, "Cardiology");
        return new Appointment(1, LocalDateTime.now().plusDays(1), AppointmentStatus.CONFIRMED, patient, doctor);
    }

    @Test
    void sendsAnEmailToThePatientsAddressWhenOneIsOnFile() {
        Patient patient = new Patient(1, "Alex Rivera", 40);
        patient.setEmail("alex.rivera@example.com");

        RecordingMailSender mailSender = new RecordingMailSender();
        SmtpEmailNotifier notifier = new SmtpEmailNotifier(mailSender, FROM_ADDRESS);

        notifier.send(appointmentFor(patient));

        assertEquals("alex.rivera@example.com", mailSender.lastMessage.getTo()[0]);
        assertEquals(FROM_ADDRESS, mailSender.lastMessage.getFrom());
    }

    @Test
    void skipsSendingWhenThePatientHasNoEmailOnFile() {
        Patient patient = new Patient(1, "Alex Rivera", 40);
        // email left null

        RecordingMailSender mailSender = new RecordingMailSender();
        SmtpEmailNotifier notifier = new SmtpEmailNotifier(mailSender, FROM_ADDRESS);

        notifier.send(appointmentFor(patient));

        assertNull(mailSender.lastMessage);
    }

    @Test
    void skipsSendingWhenThePatientsEmailIsBlank() {
        Patient patient = new Patient(1, "Alex Rivera", 40);
        patient.setEmail("   ");

        RecordingMailSender mailSender = new RecordingMailSender();
        SmtpEmailNotifier notifier = new SmtpEmailNotifier(mailSender, FROM_ADDRESS);

        notifier.send(appointmentFor(patient));

        assertNull(mailSender.lastMessage);
    }
}
