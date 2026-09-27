package lab.java.demo.dto;

import lab.java.demo.Models.Receptionist;

public class ReceptionistResponse {

    private final int id;
    private final String name;
    private final int age;
    private final boolean availability;

    public ReceptionistResponse(int id, String name, int age, boolean availability) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.availability = availability;
    }

    public static ReceptionistResponse from(Receptionist receptionist) {
        return new ReceptionistResponse(
                receptionist.getId(), receptionist.getName(), receptionist.getAge(),
                receptionist.isAvailability());
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public boolean isAvailability() { return availability; }
}
