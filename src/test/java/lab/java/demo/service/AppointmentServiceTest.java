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
import lab.java.demo.Models.AppointmentStatus;
import lab.java.demo.Models.Doctor;
import lab.java.demo.Models.NotificationChannel;
import lab.java.demo.Models.Notifier;
import lab.java.demo.Models.Patient;
import lab.java.demo.exception.AppointmentConflictException;
import lab.java.demo.exception.AppointmentNotFoundException;
import lab.java.demo.exception.DoctorNotFoundException;
import lab.java.demo.exception.InvalidAppointmentException;
import lab.java.demo.exception.InvalidAppointmentTransitionException;
import lab.java.demo.exception.PatientNotFoundException;
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
    private PatientService patientService;
    private DoctorService doctorService;
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

        patientService = new PatientService(patientRepository);
        doctorService = new DoctorService(doctorRepository);
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

        // EMAIL/SMS resolve to ConsoleEmailNotifier/ConsoleSMSNotifier, which
        // just print (Issue 16 renamed them to be honest about that); this
        // only asserts the channel-based entry point wires up and runs end
        // to end without throwing.
        appointmentService.sendRemindersForTomorrow(List.of(NotificationChannel.EMAIL, NotificationChannel.SMS));
    }

    // ----- Issue 24: appointment creation, transitions, and lookups -----

    @Test
    void createAppointmentSucceedsAndStartsAsRequested() {
        Appointment appt = appointmentService.createAppointment(patient.getId(), doctor.getId(), tomorrowAt(9));

        assertEquals(AppointmentStatus.REQUESTED, appt.getStatus());
        assertEquals(patient.getId(), appt.getPatient().getId());
        assertEquals(doctor.getId(), appt.getDoctor().getId());
    }

    @Test
    void createAppointmentRejectsAPastDateTime() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

        assertThrows(InvalidAppointmentException.class,
                () -> appointmentService.createAppointment(patient.getId(), doctor.getId(), yesterday));
    }

    @Test
    void createAppointmentRejectsAMissingDateTime() {
        assertThrows(InvalidAppointmentException.class,
                () -> appointmentService.createAppointment(patient.getId(), doctor.getId(), null));
    }

    @Test
    void createAppointmentRejectsAnUnknownPatient() {
        assertThrows(PatientNotFoundException.class,
                () -> appointmentService.createAppointment(404, doctor.getId(), tomorrowAt(9)));
    }

    @Test
    void createAppointmentRejectsAnUnknownDoctor() {
        assertThrows(DoctorNotFoundException.class,
                () -> appointmentService.createAppointment(patient.getId(), 404, tomorrowAt(9)));
    }

    @Test
    void createAppointmentRejectsDoubleBookingTheSameDoctorAtTheSameTime() {
        // Issue 3: AppointmentConflictException existed but was never thrown.
        LocalDateTime slot = tomorrowAt(9);
        appointmentService.createAppointment(patient.getId(), doctor.getId(), slot);

        assertThrows(AppointmentConflictException.class,
                () -> appointmentService.createAppointment(patient.getId(), doctor.getId(), slot));
    }

    @Test
    void doubleBookingCheckIgnoresCancelledAppointments() {
        LocalDateTime slot = tomorrowAt(9);
        Appointment first = appointmentService.createAppointment(patient.getId(), doctor.getId(), slot);
        appointmentService.cancelAppointment(first.getId());

        // A cancelled slot frees up the doctor -- this should succeed, not
        // throw AppointmentConflictException.
        Appointment second = appointmentService.createAppointment(patient.getId(), doctor.getId(), slot);
        assertEquals(AppointmentStatus.REQUESTED, second.getStatus());
    }

    @Test
    void confirmMovesARequestedAppointmentToConfirmed() {
        Appointment appt = appointmentService.createAppointment(patient.getId(), doctor.getId(), tomorrowAt(9));

        Appointment confirmed = appointmentService.confirmAppointment(appt.getId());
        assertEquals(AppointmentStatus.CONFIRMED, confirmed.getStatus());
    }

    @Test
    void confirmingAnAlreadyConfirmedAppointmentIsRejected() {
        // Issue 11: confirm()/cancel() used to always succeed regardless of
        // the current status.
        Appointment appt = appointmentService.createAppointment(patient.getId(), doctor.getId(), tomorrowAt(9));
        appointmentService.confirmAppointment(appt.getId());

        assertThrows(InvalidAppointmentTransitionException.class,
                () -> appointmentService.confirmAppointment(appt.getId()));
    }

    @Test
    void cancelMovesARequestedAppointmentToCanceled() {
        Appointment appt = appointmentService.createAppointment(patient.getId(), doctor.getId(), tomorrowAt(9));

        Appointment cancelled = appointmentService.cancelAppointment(appt.getId());
        assertEquals(AppointmentStatus.CANCELED, cancelled.getStatus());
    }

    @Test
    void cancellingAnAlreadyCancelledAppointmentIsRejected() {
        Appointment appt = appointmentService.createAppointment(patient.getId(), doctor.getId(), tomorrowAt(9));
        appointmentService.cancelAppointment(appt.getId());

        assertThrows(InvalidAppointmentTransitionException.class,
                () -> appointmentService.cancelAppointment(appt.getId()));
    }

    @Test
    void confirmingAnUnknownAppointmentThrowsNotFound() {
        assertThrows(AppointmentNotFoundException.class, () -> appointmentService.confirmAppointment(404));
    }

    @Test
    void getAppointmentsForPatientReturnsOnlyThatPatientsAppointments() {
        Patient otherPatient = patientService.registerPatient(new Patient(Patient.nextId(), "Blair Chen", 30));
        Appointment mine = appointmentService.createAppointment(patient.getId(), doctor.getId(), tomorrowAt(9));
        appointmentService.createAppointment(otherPatient.getId(), doctor.getId(), tomorrowAt(11));

        List<Appointment> result = appointmentService.getAppointmentsForPatient(patient.getId());
        assertEquals(1, result.size());
        assertEquals(mine.getId(), result.get(0).getId());
    }

    @Test
    void getAppointmentsForDoctorSortedByDateOrdersEarliestFirst() {
        Appointment later = appointmentService.createAppointment(patient.getId(), doctor.getId(), tomorrowAt(15));
        Appointment earlier = appointmentService.createAppointment(patient.getId(), doctor.getId(), tomorrowAt(8));

        List<Appointment> result = appointmentService.getAppointmentsForDoctorSortedByDate(doctor.getId());
        assertEquals(earlier.getId(), result.get(0).getId());
        assertEquals(later.getId(), result.get(1).getId());
    }
}
