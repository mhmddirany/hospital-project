package lab.java.demo.Models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demo/console stand-in for a real SMS integration: it logs the reminder
 * instead of calling an actual SMS provider. Renamed from SMSNotifier
 * (Issue 16) so the class name no longer claims to send a real text message
 * -- it never did. A real deployment would add a separate Notifier
 * implementation backed by an SMS provider API and register it in
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
public class ConsoleSMSNotifier implements Notifier {

    private static final Logger log = LoggerFactory.getLogger(ConsoleSMSNotifier.class);

    @Override
    public void send(Appointment appt) {
        log.info("[SMS] Reminder sent for appointment id={} (status={})", appt.getId(), appt.getStatus());
        log.debug("[SMS] Reminder detail: Appt #{} with Dr. {} at {} for {}",
                appt.getId(),
                appt.getDoctor().getName(),
                appt.getDateTime(),
                appt.getPatient().getName());
    }
}
