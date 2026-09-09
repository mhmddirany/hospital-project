package lab.java.demo.util;

import lab.java.demo.Models.Nurse;

import java.util.Comparator;

public final class NurseComparators {

    private NurseComparators() {}

    public static Comparator<Nurse> byName() {
        return Comparator.comparing(Nurse::getName, String.CASE_INSENSITIVE_ORDER);
    }

    public static Comparator<Nurse> byDepartmentThenName() {
        return Comparator
                .comparing(Nurse::getDepartment, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Nurse::getName, String.CASE_INSENSITIVE_ORDER);
    }
}
