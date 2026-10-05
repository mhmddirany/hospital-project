package lab.java.demo.Models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Demo/console stand-in for a real SMS integration: it logs the reminder
 * instead of calling an actual SMS provider. Renamed from SMSNotifier
 * (Issue 16) so the class name no longer claims to send a real text message
 * -- it never did.
 *
 * <p>Issue 16 follow-up: a real implementation now exists too --
 * {@link lab.java.demo.notification.TwilioSmsNotifier}. Which one
 * AppointmentService actually gets wired to depends on
 * {@code app.notifications.sms.provider} (application.properties): this
 * class is the default ({@code console}, or the property unset
 * entirely), {@code twilio} switches to the real one. Both are
 * registered under the same {@code "smsNotifier"} bean name so exactly
 * one is ever a candidate at a time -- see AppointmentService's
 * constructor.
 *
 * <p>Issue 22: this used to System.out.printf the patient's and doctor's
 * full names on every reminder, which is exactly the kind of PHI that
 * shouldn't end up in an operational log. Neither the INFO line nor the
 * DEBUG line below includes a name -- both identify people only by id, so
 * enabling DEBUG in production does not expose any PHI. A real provider
 * integration would put patient-facing content (names included) in the
 * message it sends directly to the patient, not in the server's logs.
 */
@Component("smsNotifier")
@ConditionalOnProperty(name = "app.notifications.sms.provider", havingValue = "console", matchIfMissing = true)
public class ConsoleSMSNotifier implements Notifier {

    private static final Logger log = LoggerFactory.getLogger(ConsoleSMSNotifier.class);

    @Override
    public void send(Appointment appt) {
        log.info("[SMS] Reminder sent for appointment id={} (status={})", appt.getId(), appt.getStatus());
        log.debug(
                "[SMS] Reminder detail: appointmentId={}, patientId={}, doctorId={}, dateTime={}",
                appt.getId(),
                appt.getPatient().getId(),
                appt.getDoctor().getId(),
                appt.getDateTime());
    }
}
