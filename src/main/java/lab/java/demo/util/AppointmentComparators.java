package lab.java.demo.util;

import lab.java.demo.Models.Appointment;

import java.util.Comparator;

public final class AppointmentComparators {

    private AppointmentComparators() {}

    public static Comparator<Appointment> byDateTime() {
        return Comparator.comparing(Appointment::getDateTime);
    }

    public static Comparator<Appointment> byDoctorNameThenDate() {
        return Comparator
                .comparing((Appointment a) -> a.getDoctor().getName())
                .thenComparing(Appointment::getDateTime);
    }
}
