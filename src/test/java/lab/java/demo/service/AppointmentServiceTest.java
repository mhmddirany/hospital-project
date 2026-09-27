package lab.java.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lab.java.demo.Models.Appointment;
import lab.java.demo.Models.Doctor;
import lab.java.demo.Models.NotificationChannel;
import lab.java.demo.Models.Notifier;
import lab.java.demo.Models.Patient;
import lab.java.demo.repository.AppointmentRepository;
import lab.java.demo.repository.DoctorRepository;
import lab.java.demo.repository.PatientRepository;

/**
 * Plain-object test: every collaborator here is a hand-rolled in-memory
 * class with no Spring-specific behavior, so there's no need for a Spring
 * context or mocks -- constructing the real objects directly is simpler and
 * exercises the real code path.
 */
class AppointmentServiceTest {

    private AppointmentService appointmentService;
    private Patient patient;
    private Doctor doctor;

    // A Notifier test double that just records which appointments it was
    // asked to remind, so the test can assert on exactly those.
    private static class RecordingNotifier implements Notifier {
        final List<Integer> remindedAppointmentIds = new ArrayList<>();

        @Override
        public void send(Appointment appt) {
            remindedAppointmentIds.add(appt.getId());
        }
    }

    @BeforeEach
    void setUp() {
        PatientRepository patientRepository = new PatientRepository();
        DoctorRepository doctorRepository = new DoctorRepository();
        AppointmentRepository appointmentRepository = new AppointmentRepository();

        PatientService patientService = new PatientService(patientRepository);
        DoctorService doctorService = new DoctorService(doctorRepository);
        appointmentService = new AppointmentService(appointmentRepository, patientService, doctorService);

        patient = patientService.registerPatient(new Patient(Patient.nextId(), "Alex Rivera", 40));
        doctor = doctorService.addDoctor(new Doctor(Doctor.nextId(), "Dr. Kim", 50, "Cardiology"));
    }

    private LocalDateTime tomorrowAt(int hour) {
        return LocalDate.now(ZoneId.systemDefault()).plusDays(1).atTime(hour, 0);
    }

    @Test
    void remindsOnlyConfirmedAppointmentsTomorrow() {
        Appointment confirmed = appointmentService.createAppointment(patient.getId(), doctor.getId(), tomorrowAt(9));
        appointmentService.confirmAppointment(confirmed.getId());

        Appointment cancelled = appointmentService.createAppointment(patient.getId(), doctor.getId(), tomorrowAt(11));
        appointmentService.cancelAppointment(cancelled.getId());

        Appointment stillRequested = appointmentService.createAppointment(patient.getId(), doctor.getId(), tomorrowAt(13));
        // left as REQUESTED -- never confirmed

        RecordingNotifier notifier = new RecordingNotifier();
        appointmentService.remindAppointmentsTomorrow(List.of(notifier));

        assertEquals(1, notifier.remindedAppointmentIds.size());
        assertTrue(notifier.remindedAppointmentIds.contains(confirmed.getId()));
        assertFalse(notifier.remindedAppointmentIds.contains(cancelled.getId()));
        assertFalse(notifier.remindedAppointmentIds.contains(stillRequested.getId()));
    }

    @Test
    void skipsConfirmedAppointmentsOutsideTomorrowsWindow() {
        // Two calendar days out, not "later today": robust regardless of what
        // time of day the test happens to run at (near-midnight "today +
        // a couple hours" can accidentally roll into tomorrow).
        LocalDateTime dayAfterTomorrow = LocalDate.now(ZoneId.systemDefault()).plusDays(2).atTime(9, 0);
        Appointment confirmedLater = appointmentService.createAppointment(
                patient.getId(), doctor.getId(), dayAfterTomorrow);
        appointmentService.confirmAppointment(confirmedLater.getId());

        RecordingNotifier notifier = new RecordingNotifier();
        appointmentService.remindAppointmentsTomorrow(List.of(notifier));

        assertTrue(notifier.remindedAppointmentIds.isEmpty());
    }

    @Test
    void rejectsEmptyOrMissingChannels() {
        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.sendRemindersForTomorrow(List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.sendRemindersForTomorrow(null));
    }

    @Test
    void resolvesEachRequestedChannelToItsNotifierWithoutError() {
        Appointment confirmed = appointmentService.createAppointment(patient.getId(), doctor.getId(), tomorrowAt(9));
        appointmentService.confirmAppointment(confirmed.getId());

        // EMAIL/SMS resolve to the real EmailNotifier/SMSNotifier (they just
        // print today -- see Issue 16); this only asserts the channel-based
        // entry point wires up and runs end to end without throwing.
        appointmentService.sendRemindersForTomorrow(List.of(NotificationChannel.EMAIL, NotificationChannel.SMS));
    }
}
