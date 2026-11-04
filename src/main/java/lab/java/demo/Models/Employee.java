package lab.java.demo.Models;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

/**
 * Shared fields for staff entities (Doctor, Nurse, Receptionist).
 * {@code @MappedSuperclass} means there is no "employees" table: each
 * subclass gets its own table that simply includes these columns.
 */
@MappedSuperclass
public abstract class Employee {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int age;

    /** Required by JPA; not for application code. */
    protected Employee() {
    }

    protected Employee(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}
