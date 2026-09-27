package lab.java.demo.Models;

/**
 * The notification channels a caller can ask for when requesting reminders.
 * A closed set the client can only select from, as opposed to the old
 * List<Notifier> request body, which asked clients to submit JSON that would
 * somehow deserialize into an arbitrary Notifier implementation.
 */
public enum NotificationChannel {
    EMAIL,
    SMS
}
