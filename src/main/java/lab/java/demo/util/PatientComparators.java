package lab.java.demo.util;

import lab.java.demo.Models.Patient;

import java.util.Comparator;

public final class PatientComparators {

    private PatientComparators() {}

    public static Comparator<Patient> byName() {
        return Comparator.comparing(Patient::getName, String.CASE_INSENSITIVE_ORDER);
    }

    public static Comparator<Patient> byAgeDescending() {
        return Comparator.comparingInt(Patient::getAge).reversed();
    }
}
