package lab.java.demo.Models;

/**
 * A channel that can deliver an appointment reminder. AppointmentService
 * resolves a NotificationChannel to a Notifier implementation in its
 * toNotifier() method. The two implementations shipped here
 * (ConsoleEmailNotifier, ConsoleSMSNotifier) are demo stand-ins that print
 * to standard output rather than calling a real provider -- see Issue 16.
 * A real deployment adds another implementation of this interface and
 * wires it in there, rather than changing this contract.
 */
public interface Notifier {
    void send(Appointment appt);
}
