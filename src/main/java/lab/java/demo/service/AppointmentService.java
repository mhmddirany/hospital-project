package lab.java.demo.service;

import lab.java.demo.Models.Appointment;
import lab.java.demo.Models.Doctor;
import lab.java.demo.Models.Notifier;
import lab.java.demo.Models.Patient;
import lab.java.demo.exception.AppointmentNotFoundException;
import lab.java.demo.exception.InvalidAppointmentException;
import lab.java.demo.repository.AppointmentRepository;
import lab.java.demo.util.AppointmentComparators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;

    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public Appointment createAppointment(Patient patient, Doctor doctor, LocalDateTime dateTime) {
        if (patient == null || doctor == null) {
            throw new InvalidAppointmentException("Patient and doctor must not be null");
        }
        if (dateTime == null || dateTime.isBefore(LocalDateTime.now())) {
            throw new InvalidAppointmentException("Appointment date/time must be in the future");
        }

        Appointment appt = new Appointment(
                Appointment.nextId(),
                dateTime,
                "requested",
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
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        log.info("Sending reminders for appointments between {} and {}", now, tomorrow);

        for (Appointment appt : appointmentRepository.findAll()) {
            if (appt.getDateTime().isAfter(now) && appt.getDateTime().isBefore(tomorrow)) {
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
