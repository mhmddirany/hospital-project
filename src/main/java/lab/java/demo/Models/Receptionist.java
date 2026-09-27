package lab.java.demo.Models;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Receptionist extends Employee implements Comparable<Receptionist> {
    private static final AtomicInteger ID = new AtomicInteger(1);

    private final int id;
    private boolean availability = true;

    public Receptionist(int id, String name, int age) {
        super(name, age);
        this.id = id;
    }

    public static int nextId() { return ID.getAndIncrement(); }

    public int getId() { return id; }
    public boolean isAvailability() { return availability; }
    public void setAvailability(boolean availability) { this.availability = availability; }

    public boolean checkAvailability(int doctorId) { return true; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Receptionist)) return false;
        Receptionist that = (Receptionist) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Receptionist other) {
        int cmp = this.getName().compareToIgnoreCase(other.getName());
        if (cmp != 0) return cmp;
        return Integer.compare(this.id, other.id);
    }
}
