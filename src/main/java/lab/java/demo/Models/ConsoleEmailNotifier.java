package lab.java.demo.Models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demo/console stand-in for a real email integration: it logs the reminder
 * instead of calling an actual email provider. Renamed from EmailNotifier
 * (Issue 16) so the class name no longer claims to send real email -- it
 * never did. A real deployment would add a separate Notifier implementation
 * backed by an SMTP relay or provider API and register it in
 * AppointmentService.toNotifier() instead of changing this one.
 *
 * <p>Issue 22: this used to System.out.printf the patient's and doctor's
 * full names on every reminder, which is exactly the kind of PHI that
 * shouldn't end up in an operational log. The INFO line below only
 * identifies the appointment and channel; the human-readable message
 * (names included) is logged at DEBUG, which is off by default in
 * production -- a real provider integration would put that content in the
 * message it sends directly to the patient, not in the server's logs.
 */
public class ConsoleEmailNotifier implements Notifier {

    private static final Logger log = LoggerFactory.getLogger(ConsoleEmailNotifier.class);

    @Override
    public void send(Appointment appt) {
        log.info("[Email] Reminder sent for appointment id={} (status={})", appt.getId(), appt.getStatus());
        log.debug("[Email] Reminder detail: Appointment #{} with Dr. {} for {} at {} (status={})",
                appt.getId(),
                appt.getDoctor().getName(),
                appt.getPatient().getName(),
                appt.getDateTime(),
                appt.getStatus());
    }
}
