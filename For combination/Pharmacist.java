import java.util.List;
import java.util.Map;
import java.util.Scanner;

//import javax.print.Doc;

public class Pharmacist extends User {
    private static Scanner sc = new Scanner(System.in);

    public Pharmacist(String userID, String name, String gender) {
        super(userID, null, name, gender, "Pharmacist"); // Default password is null
    }

    public String getUserID() {
        return userID;
    }

    public boolean viewAppointmentOutcomeRecord(List<Patient> patients) {
        System.out.println("\nCompleted Appointments:");
    
        boolean hasCompletedAppointments = false;
    
        for (Patient patient : patients) {
            for (Appointment appointment : patient.getAppointments()) {
                if (appointment.getStatus().equals("Completed")) {
                    hasCompletedAppointments = true;
                    System.out.println(
                        "Date: " + appointment.getDateTime().toLocalDate() +
                        "\nTime: " + appointment.getDateTime().toLocalTime() +
                        "\nPatient: " + patient.getName() +
                        "\nAppointment ID: " + appointment.getAppointmentID() +
                        "\nConsultation Notes: " + (appointment.getConsultationNotes() != null ? appointment.getConsultationNotes() : "None") +
                        "\nPrescribed Medication: " + (appointment.getPrescribedMedication() != null && !appointment.getPrescribedMedication().isEmpty()
                        ? getPrescribedMedicationDetails(appointment.getPrescribedMedication())
                        : "None") +
                        "\nMedication Status: " + (appointment.getMedicationStatus() != null ? appointment.getMedicationStatus() : "Not Prescribed")
                    );
                }
            }
        }
    
        if (!hasCompletedAppointments) {
            System.out.println("No completed appointments found.");
        }

        return hasCompletedAppointments;
    }

    private static String getPrescribedMedicationDetails(Map<Medication, Integer> prescribedMedication) {
        StringBuilder medicationDetails = new StringBuilder();
        for (Map.Entry<Medication, Integer> entry : prescribedMedication.entrySet()) {
            medicationDetails.append(entry.getKey().getName()) // Assuming Medication has a getName() method
                             .append(" (Quantity: ")
                             .append(entry.getValue())
                             .append("), ");
        }
        // Remove the trailing comma and space, if any
        if (medicationDetails.length() > 0) {
            medicationDetails.setLength(medicationDetails.length() - 2);
        }
        return medicationDetails.toString();
    }
    
    
    public void updatePrescriptionStatus(List<Patient> patients, Inventory inventory) {
        boolean hasCompletedAppointments = viewAppointmentOutcomeRecord(patients);

        if (!hasCompletedAppointments) {
            System.out.println("No completed appointments available.");
            return; // Exit early if no completed appointments
        }
    
        // Prompt for appointment ID
        System.out.println("Enter Appointment ID to update status: ");
        String appointmentID = sc.next();
        
        
        Appointment targetAppointment = null;
    
        // Find the target appointment by Appointment ID
        for (Patient patient : patients) {
            for (Appointment appointment : patient.getAppointments()) {
                if (appointment.getAppointmentID().equals(appointmentID)) {
                    targetAppointment = appointment;
                    break;
                }
            }
            if (targetAppointment != null) {
                break;
            }
        }
    
        if (targetAppointment == null) {
            System.out.println("Appointment not found.");
            return;
        }
    
        // Check the medication status
        if ("Dispense Complete".equals(targetAppointment.getMedicationStatus())) 
        {
            System.out.println("Medication already dispensed.");
        } else if ("Pending to Dispense".equals(targetAppointment.getMedicationStatus())) 
        {
            System.out.println("Pending medication prescription found.");
            System.out.println("Do you want to dispense medications? (1. Yes / 2. No)");
            int choice = sc.nextInt();
    
            if (choice == 1) {
                Map<Medication, Integer> medications = targetAppointment.getPrescribedMedication();
    
                if (medications.isEmpty()) {
                    System.out.println("No medications prescribed for this appointment.");
                    return;
                }
    
                for (Map.Entry<Medication, Integer> entry : medications.entrySet()) {
                    Medication medication = entry.getKey();
                    int quantity = entry.getValue();
    
                    if (quantity > medication.getQuantity()) {
                        System.out.println("Insufficient Medication: " + medication.getName() + " in Inventory.");
                        return;
                    } else {
                        System.out.println("Dispensing Medication: " + medication.getName() + " (Quantity: " + quantity + ")");
                        medication.updateQuantity(medication.getQuantity() - quantity);
                    }
                }
    
                // Mark medication status as dispensed
                targetAppointment.completeDispense();
                System.out.println("Medication dispensed successfully. Status updated to 'Dispense Complete'.");
            } else {
                System.out.println("Dispensing canceled.");
            }
        } else {
            System.out.println("No pending medication prescriptions for this appointment.");
        }
    }

    // Method to view the current medication inventory
    public void viewInventory(Inventory inventory) {
        System.out.println("Viewing medication inventory:");
        inventory.displayInventory(false);
    }

    public void submitReplenishmentRequest(Inventory inventory) {
        inventory.displayInventory(false);
        System.out.println("Submit Replenishment Request for Medication ID: ");
        String medicationID = sc.next();
        System.out.println("Amount to replenish: ");
        int replenishAmount = sc.nextInt();
        inventory.newReplenishmentRequest(medicationID, replenishAmount);
    }

    // for the administrator function
    public void updateRole(Pharmacist pharmacist, int newRole, List<Doctor> doctors, List<Administrator> administrators) {
        switch (newRole) {
            case 1:
                this.role = "Doctor";
                System.out.println("Enter Doctor Specialty: ");
                sc.nextLine();
                String specialty = sc.nextLine();
                Doctor doctor = new Doctor(pharmacist.getuserID(), pharmacist.getName(), pharmacist.getGender(), specialty);
                doctors.add(doctor);
                break;
            case 3:
                this.role = "Administrator";
                Administrator administrator = new Administrator(pharmacist.getuserID(), pharmacist.getName(), pharmacist.getGender());
                administrators.add(administrator);
                break;
            default:
                System.out.println("Invalid option. Please try again.");
                return;
        }
    }

    // Find Pharmacist by ID
    public static Pharmacist findPharmacistByID(String pharmacistID, List<Pharmacist> pharmacists) {
        for (Pharmacist pharmacist : pharmacists) {
            if (pharmacist.getuserID().equals(pharmacistID)) {
                return pharmacist;
            }
        }
        return null;
    }

    @Override
    public void displayMenu() {
        System.out.println("""
                
                Pharmacist Display Menu: 
                1. View Appointment Outcome Record
                2. Update Prescription Status
                3. View Medication Inventory
                4. Submit Replenishment Request
                5. Logout
                Choose options (1-5): """);

    }
}

