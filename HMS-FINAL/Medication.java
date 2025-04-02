import java.io.Serializable;
import java.util.List;

public class Medication implements Serializable {
    private static final long serialVersionUID = 1L;
    private String medicationID;
    private String name;
    private int quantity;
    private int replenishmentRequest;
    private int lowStockAlert;

    public Medication(String medicationID, String name, int quantity, int lowStockAlert) {
        this.medicationID = medicationID;
        this.name = name;
        this.quantity = quantity;
        this.replenishmentRequest = 0; // default
        this.lowStockAlert = lowStockAlert;
    }

    public String getName() {
        return this.name;
    }

    public String getMedicationID() {
        return this.medicationID;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public int getReplenishmentRequest() {
        return this.replenishmentRequest;
    }

    public int getLowStockAlert() {
        return this.lowStockAlert;
    }

    public void updateQuantity(int newQuantity) {
        this.quantity = newQuantity;
    }

    public void updateLowStockAlert(int newQuantity) {
        this.lowStockAlert = newQuantity;
    }

    public void updateReplenishmentRequest(int replenishmentAmount) {
        this.replenishmentRequest = replenishmentAmount;
    }

    public boolean getLevelAlert() {
        if (this.quantity < this.getLowStockAlert()) {
            return true;
        }
        
        return false;
    }

    // Find Medication by ID
    public static Medication findMedicationByID(String medicationID, List<Medication> medications) {
        for (Medication medication : medications) {
            if (medication.getMedicationID().equals(medicationID)) {
                return medication;
            }
        }
        return null;
    }

}
