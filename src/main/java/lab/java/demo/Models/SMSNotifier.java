package lab.java.demo.Models; 
public class SMSNotifier implements Notifier {
    @Override
    public void send(Appointment appt) {
        System.out.printf("[SMS] Reminder: Appt #%d with Dr. %s at %s for %s%n",
                appt.getId(),
                appt.getDoctor().getName(),
                appt.getDateTime(),
                appt.getPatient().getName());
    }
}
