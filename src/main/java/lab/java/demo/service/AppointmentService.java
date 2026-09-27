package lab.java.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import lab.java.demo.Models.Appointment;
import lab.java.demo.Models.AppointmentStatus;
import lab.java.demo.Models.Doctor;
import lab.java.demo.Models.Notifier;
import lab.java.demo.Models.Patient;
import lab.java.demo.exception.AppointmentConflictException;
import lab.java.demo.exception.AppointmentNotFoundException;
import lab.java.demo.exception.DoctorNotFoundException;
import lab.java.demo.exception.InvalidAppointmentException;
import lab.java.demo.repository.AppointmentRepository;
import lab.java.demo.util.AppointmentComparators;

@Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    // Explicit zone for "tomorrow" -- reminders are about calendar days for
    // this clinic, not an implicit JVM default that could differ by
    // deployment environment.
    private static final ZoneId REMINDER_ZONE = ZoneId.systemDefault();

    private final AppointmentRepository appointmentRepository;
    private final PatientService patientService;
    private final DoctorService doctorService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                               PatientService patientService,
                               DoctorService doctorService) {
        this.appointmentRepository = appointmentRepository;
        this.patientService = patientService;
        this.doctorService = doctorService;
    }

    public Appointment createAppointment(int patientId, int doctorId, LocalDateTime dateTime) {
        if (dateTime == null || dateTime.isBefore(LocalDateTime.now())) {
            throw new InvalidAppointmentException("Appointment date/time must be in the future");
        }

        Patient patient = patientService.getByIdOrThrow(patientId);
        Doctor doctor = doctorService.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException(doctorId));

        boolean doctorAlreadyBooked = appointmentRepository.findByDoctorId(doctor.getId()).stream()
                .anyMatch(existing -> existing.getStatus() != AppointmentStatus.CANCELED
                        && existing.getDateTime().isEqual(dateTime));
        if (doctorAlreadyBooked) {
            throw new AppointmentConflictException(doctor.getId(), dateTime.toString());
        }

        Appointment appt = new Appointment(
                Appointment.nextId(),
                dateTime,
                AppointmentStatus.REQUESTED,
                patient,
                doctor
        );

        appointmentRepository.save(appt);
        log.info("Created appointment id={} for patientId={} with doctorId={} at {}",
                appt.getId(), patient.getId(), doctor.getId(), dateTime);
        return appt;
    }

    public Appointment getAppointmentOrThrow(int apptId) {
        return appointmentRepository.findById(apptId)
                .orElseThrow(() -> {
                    log.warn("Appointment with id={} not found", apptId);
                    return new AppointmentNotFoundException(apptId);
                });
    }

    public Appointment confirmAppointment(int apptId) {
        Appointment appt = getAppointmentOrThrow(apptId);
        appt.confirm();
        appointmentRepository.save(appt);
        log.info("Confirmed appointment id={}", apptId);
        return appt;
    }

    public Appointment cancelAppointment(int apptId) {
        Appointment appt = getAppointmentOrThrow(apptId);
        appt.cancel();
        appointmentRepository.save(appt);
        log.info("Cancelled appointment id={}", apptId);
        return appt;
    }

    public List<Appointment> getAppointmentsForPatient(int patientId) {
        List<Appointment> list = appointmentRepository.findByPatientId(patientId);
        log.debug("Found {} appointments for patientId={}", list.size(), patientId);
        return list;
    }

    public List<Appointment> getAppointmentsForDoctorSortedByDate(int doctorId) {
        List<Appointment> appts = appointmentRepository.findByDoctorId(doctorId);
        appts.sort(AppointmentComparators.byDateTime());
        log.debug("Found {} appointments for doctorId={} (sorted by date)", appts.size(), doctorId);
        return appts;
    }

    public void sendRemindersForTomorrow(List<Notifier> notifiers) {
        // "Tomorrow" means the next calendar day, not now..now+24h -- running
        // this at 3pm should not catch today's 4pm appointment or miss
        // tomorrow's 2pm one. Half-open window: [start of tomorrow, start of
        // the day after tomorrow).
        LocalDate tomorrow = LocalDate.now(REMINDER_ZONE).plusDays(1);
        LocalDateTime windowStart = tomorrow.atStartOfDay();
        LocalDateTime windowEnd = tomorrow.plusDays(1).atStartOfDay();
        log.info("Sending reminders for appointments in [{}, {})", windowStart, windowEnd);

        for (Appointment appt : appointmentRepository.findAll()) {
            LocalDateTime dateTime = appt.getDateTime();
            if (!dateTime.isBefore(windowStart) && dateTime.isBefore(windowEnd)) {
                log.debug("Sending reminder for appointment id={}", appt.getId());
                appt.remind(notifiers);
            }
        }
    }

    public List<Appointment> getAllAppointmentsSortedByDoctorThenDate() {
        List<Appointment> all = appointmentRepository.findAll();
        all.sort(AppointmentComparators.byDoctorNameThenDate());
        log.debug("Returning {} appointments sorted by doctor then date", all.size());
        return all;
    }
}
