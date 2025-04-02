import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Appointment implements Serializable{
    private static final long serialVersionUID = 1L;
    private String appointmentID;
    private String patientID;
    private String doctorID;
    private LocalDateTime dateTime;
    private String status;
    private Map<Medication, Integer> prescribedMedication;
    private String medicationStatus;
    private String consultationNotes;
    private static Scanner sc = new Scanner(System.in);

    public Appointment(String appointmentID, String patientID, String doctorID, LocalDateTime dateTime, String comments) {
        this.appointmentID = appointmentID;
        this.patientID = patientID;
        this.doctorID = doctorID;
        this.dateTime = dateTime;
        this.status = "Pending";
        this.prescribedMedication = new HashMap<>();
        this.medicationStatus = null;
        this.consultationNotes = null;
        //if (comments != null && !comments.isEmpty()) {
            //this.consultationNotes = comments; // Use consultationNotes for initial comments
        //}
    }
    
    public String getAppointmentID() {
        return appointmentID;
    }

    public String getDoctorID() {
        return doctorID;
    }

    public String getPatientID(){
        return patientID;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getMedicationStatus() {
        return medicationStatus;
    }

    public Map<Medication, Integer> getPrescribedMedication() {
        return this.prescribedMedication;
    }

    public String getConsultationNotes(){
        return consultationNotes;
    }
    
    public void confirm() {
        if (!status.equals("Pending")) {
            System.out.println("Only pending appointments can be confirmed.");
            return;
        }
        this.status = "Confirmed";
    }
    
    public void cancel() {
        if (status.equals("Completed")) {
            System.out.println("Completed appointments cannot be canceled.");
            return;
        }
        if (status.equals("Cancelled")) {
            System.out.println("Appointment is already canceled.");
            return;
        }
        this.status = "Cancelled";
    }
    
    public void complete(Inventory inventory, String appointmentID) {
        if (!status.equals("Confirmed")) {
            System.out.println("Only confirmed appointments can be completed.");
            return;
        }

        System.out.println("""
                Prescribe Medication for Appointment (if any): 
                1. Yes
                2. No
                Choose options (1-2): """);
        int option = sc.nextInt();
    
        switch (option) {
            case 1:
                inventory.displayInventory(false);
                System.out.println("Medication ID: ");
                String medicationID = sc.next();
                System.out.println("Quantity: ");
                int quantity = sc.nextInt();
                Medication medication = inventory.findMedicationByID(medicationID);
                if (medication == null) {
                    System.out.println("Medication Not Found. Please Try Again.");
                    return;
                }
                this.prescribedMedication.put(medication, quantity);
                this.medicationStatus = "Pending to Dispense";
                break;
            case 2:
                break;
            default:
                System.out.println("Invalid option. Please try again.");
                break;
        }
    
        System.out.println("Consultation Notes for Appointment (if any): ");
        sc.nextLine();
        this.consultationNotes = sc.nextLine();
        this.status = "Completed";
        System.out.println("Outcome recorded successfully for Appointment ID: " + appointmentID);
    }
    

    public void completeDispense() {
        this.medicationStatus = "Dispense Complete";
    }

    public String getStatus() {
        return status;
    }

    public void updateDateTime(LocalDateTime newDateTime) {
        if (!status.equals("Pending") && !status.equals("Confirmed")) {
            System.out.println("Only pending or confirmed appointments can be rescheduled.");
            return;
        }
        System.out.println("Updating appointment date and time from " + this.dateTime + " to " + newDateTime);
        this.dateTime = newDateTime;
    }
    

    @Override
    public String toString() {
        return "Appointment [Appointment ID=" + appointmentID 
            + ", Patient ID=" + patientID 
            + ", Doctor ID=" + doctorID
            + ", Date and Time=" + dateTime 
            + ", Status=" + status 
            + (medicationStatus != null ? ", Medication Status=" + medicationStatus : "")
            + (consultationNotes != null ? ", Consultation Notes=" + consultationNotes : "")
            + "]";
    }
}
