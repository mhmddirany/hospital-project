package lab.java.demo.util;

import lab.java.demo.Models.Appointment;

import java.util.*;
import java.util.stream.Collectors;

public final class CollectionUtils {

    private CollectionUtils() {}

    public static Map<String, List<Appointment>> groupAppointmentsByStatus(List<Appointment> appointments) {
        return appointments.stream()
                .collect(Collectors.groupingBy(Appointment::getStatus));
    }

    public static Set<Integer> extractPatientIds(List<Appointment> appointments) {
        Set<Integer> ids = new HashSet<>();
        for (Appointment a : appointments) {
            ids.add(a.getPatient().getId());
        }
        return ids;
    }
}
