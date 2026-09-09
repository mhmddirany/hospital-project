package lab.java.demo.Models; 
public class EmailNotifier implements Notifier {
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
