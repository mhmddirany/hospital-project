package lab.java.demo.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import lab.java.demo.Models.Appointment;
import lab.java.demo.Models.Notifier;
import lab.java.demo.Models.Patient;

/**
 * Issue 16 follow-up: a real email provider, as an alternative to the
 * {@code ConsoleEmailNotifier} demo stand-in. Active only when
 * {@code app.notifications.email.provider=smtp} (application.properties)
 * -- otherwise this bean is never created and Console remains the
 * default (see the {@code @ConditionalOnProperty} below and
 * {@code ConsoleEmailNotifier}'s Javadoc).
 *
 * <p>Sends through Spring Boot's own {@link JavaMailSender}, which is
 * auto-configured from the standard {@code spring.mail.*} properties
 * (host/port/username/password) -- the same properties that work with
 * Gmail SMTP, SendGrid's SMTP relay, AWS SES, or any other SMTP server.
 * Those aren't duplicated here; see README.md for what to set.
 *
 * <p>There's deliberately no account-level email/password on this class
 * itself: those live in {@code spring.mail.*}/env vars, read by Spring
 * Boot's own auto-configuration, consistent with "credentials supplied
 * at configuration time, not written into source" (Issue 21's ask,
 * applied here too).
 */
@Component("emailNotifier")
@ConditionalOnProperty(name = "app.notifications.email.provider", havingValue = "smtp")
public class SmtpEmailNotifier implements Notifier {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailNotifier.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public SmtpEmailNotifier(JavaMailSender mailSender,
                              @Value("${app.notifications.email.from}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public void send(Appointment appt) {
        Patient patient = appt.getPatient();
        String to = patient.getEmail();

        if (to == null || to.isBlank()) {
            // Don't fail the whole reminder batch over one patient who
            // was registered without an email address -- just skip them
            // and say why, same severity as any other "couldn't deliver
            // this one" condition.
            log.warn("Skipping email reminder for appointment id={}: patientId={} has no email on file",
                    appt.getId(), patient.getId());
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject("Appointment Reminder");
        message.setText("This is a reminder of your appointment with Dr. " + appt.getDoctor().getName()
                + " on " + appt.getDateTime() + ".");

        mailSender.send(message);
        // Ids only -- never the address or message body -- consistent
        // with Issue 22: the email itself (sent to the patient, the data
        // subject) is fine to contain their details; the server log is
        // not the place for it.
        log.info("[Email/SMTP] Reminder sent for appointment id={} (status={})", appt.getId(), appt.getStatus());
    }
}
