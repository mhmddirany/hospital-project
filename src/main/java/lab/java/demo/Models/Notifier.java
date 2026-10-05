package lab.java.demo.Models;

/**
 * A channel that can deliver an appointment reminder. AppointmentService
 * resolves a NotificationChannel to a Notifier implementation in its
 * toNotifier() method, picking between a demo stand-in and a real
 * provider per channel based on configuration (see Issue 16's follow-up):
 * <ul>
 *   <li>EMAIL: {@code ConsoleEmailNotifier} (demo, prints to the log;
 *       default) or {@code lab.java.demo.notification.SmtpEmailNotifier}
 *       (real, via {@code app.notifications.email.provider=smtp}).</li>
 *   <li>SMS: {@code ConsoleSMSNotifier} (demo; default) or
 *       {@code lab.java.demo.notification.TwilioSmsNotifier} (real, via
 *       {@code app.notifications.sms.provider=twilio}).</li>
 * </ul>
 * Adding a third provider for an existing channel means adding another
 * implementation of this interface and wiring it in under the same
 * {@code "emailNotifier"}/{@code "smsNotifier"} bean name, rather than
 * changing this contract or AppointmentService.
 */
public interface Notifier {
    void send(Appointment appt);
}
