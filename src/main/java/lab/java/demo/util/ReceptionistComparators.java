package lab.java.demo.util;

import lab.java.demo.Models.Receptionist;

import java.util.Comparator;

public final class ReceptionistComparators {

    private ReceptionistComparators() {}

    public static Comparator<Receptionist> byName() {
        return Comparator.comparing(Receptionist::getName, String.CASE_INSENSITIVE_ORDER);
    }
}
