package lab.java.demo.util;

import lab.java.demo.Models.Doctor;

import java.util.Comparator;

public final class DoctorComparators {

    private DoctorComparators() {}

    public static Comparator<Doctor> byName() {
        return Comparator.comparing(Doctor::getName, String.CASE_INSENSITIVE_ORDER);
    }

    public static Comparator<Doctor> bySpecialtyThenName() {
        return Comparator
                .comparing(Doctor::getSpecialty, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Doctor::getName, String.CASE_INSENSITIVE_ORDER);
    }
}
