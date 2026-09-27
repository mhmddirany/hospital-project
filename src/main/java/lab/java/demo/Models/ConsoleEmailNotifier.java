package lab.java.demo.Models;

/**
 * Demo/console stand-in for a real email integration: it prints the
 * reminder to standard output instead of calling an actual email provider.
 * Renamed from EmailNotifier (Issue 16) so the class name no longer claims
 * to send real email -- it never did. A real deployment would add a
 * separate Notifier implementation backed by an SMTP relay or provider API
 * and register it in AppointmentService.toNotifier() instead of changing
 * this one.
 */
public class ConsoleEmailNotifier implements Notifier {
    @Override
    public void send(Appointment appt) {
        System.out.printf("[Email] Reminder: Appointment #%d with Dr. %s for %s at %s (status=%s)%n",
                appt.getId(),
                appt.getDoctor().getName(),
                appt.getPatient().getName(),
                appt.getDateTime(),
                appt.getStatus());
    }
}
