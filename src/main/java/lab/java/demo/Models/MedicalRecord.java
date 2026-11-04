package lab.java.demo.Models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;

/**
 * A patient's medical record, stored as part of the patient
 * ({@code @Embeddable}): allergies is a column on the patients table, and
 * conditions/medications each live in their own child table keyed by
 * patient_id (see V1__initial_schema.sql). {@code @OrderColumn} keeps the
 * lists in the order they were added and lets both be loaded eagerly.
 *
 * <p>Issue 20 still holds: the lists are only exposed as unmodifiable
 * views, and only change through the add/remove methods below.
 */
@Embeddable
public class MedicalRecord {

    @Column(name = "allergies", length = 1000)
    private String allergies = "";

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "patient_conditions", joinColumns = @JoinColumn(name = "patient_id"))
    @OrderColumn(name = "list_index")
    @Column(name = "condition_name", nullable = false)
    private List<String> conditions = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "patient_medications", joinColumns = @JoinColumn(name = "patient_id"))
    @OrderColumn(name = "list_index")
    @Column(name = "medication_name", nullable = false)
    private List<String> medications = new ArrayList<>();

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }

    public List<String> getConditions() { return Collections.unmodifiableList(conditions); }
    public List<String> getMedications() { return Collections.unmodifiableList(medications); }

    public boolean addCondition(String condition) {
        if (condition == null || condition.isBlank()) {
            throw new IllegalArgumentException("condition must not be blank");
        }
        return conditions.add(condition);
    }

    public boolean removeCondition(String condition) {
        return conditions.remove(condition);
    }

    public boolean addMedication(String medication) {
        if (medication == null || medication.isBlank()) {
            throw new IllegalArgumentException("medication must not be blank");
        }
        return medications.add(medication);
    }

    public boolean removeMedication(String medication) {
        return medications.remove(medication);
    }

    // ---------- equals / hashCode ----------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MedicalRecord)) return false;
        MedicalRecord that = (MedicalRecord) o;
        return Objects.equals(allergies, that.allergies) &&
               Objects.equals(new ArrayList<>(conditions), new ArrayList<>(that.conditions)) &&
               Objects.equals(new ArrayList<>(medications), new ArrayList<>(that.medications));
    }

    @Override
    public int hashCode() {
        return Objects.hash(allergies, new ArrayList<>(conditions), new ArrayList<>(medications));
    }
}
