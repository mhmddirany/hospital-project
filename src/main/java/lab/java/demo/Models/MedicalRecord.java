package lab.java.demo.Models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MedicalRecord {
    private String allergies = "";
    private final List<String> conditions = new ArrayList<>();
    private final List<String> medications = new ArrayList<>();

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }

    public List<String> getConditions() { return conditions; }
    public List<String> getMedications() { return medications; }

    // ---------- equals / hashCode ----------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MedicalRecord)) return false;
        MedicalRecord that = (MedicalRecord) o;
        return Objects.equals(allergies, that.allergies) &&
               Objects.equals(conditions, that.conditions) &&
               Objects.equals(medications, that.medications);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allergies, conditions, medications);
    }
}
