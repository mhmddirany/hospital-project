package lab.java.demo.Models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Demo/console stand-in for a real email integration: it logs the reminder
 * instead of calling an actual email provider. Renamed from EmailNotifier
 * (Issue 16) so the class name no longer claims to send real email -- it
 * never did.
 *
 * <p>Issue 16 follow-up: a real implementation now exists too --
 * {@link lab.java.demo.notification.SmtpEmailNotifier}. Which one
 * AppointmentService actually gets wired to depends on
 * {@code app.notifications.email.provider} (application.properties):
 * this class is the default ({@code console}, or the property unset
 * entirely), {@code smtp} switches to the real one. Both are registered
 * under the same {@code "emailNotifier"} bean name so exactly one is ever
 * a candidate at a time -- see AppointmentService's constructor.
 *
 * <p>Issue 22: this used to System.out.printf the patient's and doctor's
 * full names on every reminder, which is exactly the kind of PHI that
 * shouldn't end up in an operational log. Neither the INFO line nor the
 * DEBUG line below includes a name -- both identify people only by id, so
 * enabling DEBUG in production does not expose any PHI. A real provider
 * integration would put patient-facing content (names included) in the
 * message it sends directly to the patient, not in the server's logs.
 */
@Component("emailNotifier")
@ConditionalOnProperty(name = "app.notifications.email.provider", havingValue = "console", matchIfMissing = true)
public class ConsoleEmailNotifier implements Notifier {

    private static final Logger log = LoggerFactory.getLogger(ConsoleEmailNotifier.class);

    @Override
    public void send(Appointment appt) {
        log.info("[Email] Reminder sent for appointment id={} (status={})", appt.getId(), appt.getStatus());
        log.debug(
                "[Email] Reminder detail: appointmentId={}, patientId={}, doctorId={}, dateTime={}, status={}",
                appt.getId(),
                appt.getPatient().getId(),
                appt.getDoctor().getId(),
                appt.getDateTime(),
                appt.getStatus());
    }
}
