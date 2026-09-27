package lab.java.demo.Models;

/**
 * Demo/console stand-in for a real SMS integration: it prints the reminder
 * to standard output instead of calling an actual SMS provider. Renamed
 * from SMSNotifier (Issue 16) so the class name no longer claims to send a
 * real text message -- it never did. A real deployment would add a
 * separate Notifier implementation backed by an SMS provider API and
 * register it in AppointmentService.toNotifier() instead of changing this
 * one.
 */
public class ConsoleSMSNotifier implements Notifier {
    @Override
    public void send(Appointment appt) {
        System.out.printf("[SMS] Reminder: Appt #%d with Dr. %s at %s for %s%n",
                appt.getId(),
                appt.getDoctor().getName(),
                appt.getDateTime(),
                appt.getPatient().getName());
    }
}
