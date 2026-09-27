package lab.java.demo.dto;

import lab.java.demo.Models.Nurse;

public class NurseResponse {

    private final int id;
    private final String name;
    private final int age;
    private final String department;
    private final boolean availability;

    public NurseResponse(int id, String name, int age, String department, boolean availability) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.department = department;
        this.availability = availability;
    }

    public static NurseResponse from(Nurse nurse) {
        return new NurseResponse(
                nurse.getId(), nurse.getName(), nurse.getAge(),
                nurse.getDepartment(), nurse.isAvailability());
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getDepartment() { return department; }
    public boolean isAvailability() { return availability; }
}
