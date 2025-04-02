import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.InputMismatchException;

public class Main {
    private static final String USER_DATA_FILE = "user_data.ser";
    
    private static Main instance;
    
    public Main() {
        if (instance == null) {
            instance = this; // Assign this object to the static instance
        } else {
            throw new IllegalStateException("Main instance already exists!"); // Prevent accidental re-creation
        }
    }

    public static Main getInstance() {
        if (instance == null) {
            instance = new Main(); // Lazy initialization if not created yet
        }
        return instance;
    }
    
    public List<Patient> patients = new ArrayList<>();
    public List<Doctor> doctors = new ArrayList<>();
    public List<Pharmacist> pharmacists = new ArrayList<>();
    public List<Administrator> administrators = new ArrayList<>();
    public Inventory inventory = new Inventory();
    private static SchedulingSystem schedulingSystem = new SchedulingSystem();
    private String[] roles = {"Patient", "Doctor", "Pharmacist", "Administrator"};

    public static void main(String[] args) {
        Main mainInstance = Main.getInstance();

        // Load serialized users if available, otherwise load from CSV
        mainInstance.loadUserData();

        Scanner sc = new Scanner(System.in);

        // Login and authentication process
        User currentUser = null;
        int option = 0;

        while (option != 2) {
                System.out.println("""
                    Welcome to HMS,
                    1. Login
                    2. Exit """);
            option = sc.nextInt();

            switch (option) {
                case 1: // Login
                    while (true) {
                        System.out.println("Enter User ID: ");
                        String userID = sc.next();
                        System.out.println("Enter Password: ");
                        String password = sc.next();

                        currentUser = mainInstance.authenticateUser(userID, password);

                        if (currentUser != null) {
                            System.out.println("Login successful. Welcome, " + currentUser.getName() + "!");
                            break;
                        } else {
                            System.out.println("Invalid credentials. Please try again.");
                        }
                    }
                    break;
                case 2:
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }

            // Example menu handling
            boolean loggedIn = true;
            while (loggedIn) {
                try {
                    currentUser.displayMenu();
                    int choice = sc.nextInt();

                    if (currentUser instanceof Patient) {
                        loggedIn = mainInstance.handlePatientOptions((Patient) currentUser, loggedIn, choice, sc);
                    } else if (currentUser instanceof Doctor) {
                        loggedIn = mainInstance.handleDoctorOptions((Doctor) currentUser, loggedIn, choice, sc);
                    } else if (currentUser instanceof Pharmacist) {
                        loggedIn = mainInstance.handlePharmacistOptions((Pharmacist) currentUser, loggedIn, choice, sc);
                    } else if (currentUser instanceof Administrator) {
                        loggedIn = mainInstance.handleAdminOptions((Administrator) currentUser, loggedIn, choice, sc);
                    }
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input. Please enter a valid number.");
                    sc.nextLine(); // Clear the invalid input from the scanner buffer
                }
            }

            if (!loggedIn) {
                System.out.println("Successfully logged out.");
            }        
        }

        sc.close();
        mainInstance.saveUserData(); // Save user data before exiting
    }

    // Static method to save data immediately after a change
    public static void saveDataOnChange() {
        Main.getInstance().saveUserData();
    }

    // Rest of your methods in Main remain unchanged

    // Load user data from a serialized file or, if not found, load from CSV
    @SuppressWarnings("unchecked")
    private void loadUserData() {
        File file = new File(USER_DATA_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                List<User> allUsers = (List<User>) ois.readObject();
                for (User user : allUsers) {
                    if (user instanceof Patient) patients.add((Patient) user);
                    else if (user instanceof Doctor) doctors.add((Doctor) user);
                    else if (user instanceof Pharmacist) pharmacists.add((Pharmacist) user);
                    else if (user instanceof Administrator) administrators.add((Administrator) user);
                }
                System.out.println("User data loaded successfully from file.");
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading user data: " + e.getMessage());
            }
        } else {
            // Load from CSV if no serialized data file exists
            try {
                CSVLoader.loadPatientsFromCSV("Patient_List.csv", this);
                CSVLoader.loadStaffFromCSV("Staff_List.csv", this);
                CSVLoader.loadMedicineFromCSV("Medicine_List.csv", this);
                System.out.println("User data loaded from CSV files.");
                saveUserData(); // Save to serialized file for future runs
            } catch (IOException e) {
                System.err.println("Error loading data from CSV files: " + e.getMessage());
            }
        }
    }

    // Save user data to a serialized file
    private void saveUserData() {
        List<User> allUsers = new ArrayList<>();
        allUsers.addAll(patients);
        allUsers.addAll(doctors);
        allUsers.addAll(pharmacists);
        allUsers.addAll(administrators);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USER_DATA_FILE))) {
            oos.writeObject(allUsers);
            System.out.println("User data saved successfully.");
        } catch (IOException e) {
            System.err.println("Error saving user data: " + e.getMessage());
        }
    }

    // Authentication process
    private User authenticateUser(String userID, String password) {
        for (Patient patient : patients) {
            if (patient.login(userID, password)) return patient;
        }
        for (Doctor doctor : doctors) {
            if (doctor.login(userID, password)) return doctor;
        }
        for (Pharmacist pharmacist : pharmacists) {
            if (pharmacist.login(userID, password)) return pharmacist;
        }
        for (Administrator admin : administrators) {
            if (admin.login(userID, password)) return admin;
        }
        return null;
    }

    // Handle Patient options
    private boolean handlePatientOptions(Patient patient, boolean loggedIn, int choice, Scanner sc) {
        switch (choice) {
            case 1 -> patient.viewPatientInformation();
            case 2 -> patient.updateContactInfo();
            case 3 -> patient.scheduleAppointment(schedulingSystem);
            case 4 -> patient.rescheduleAppointment(schedulingSystem);
            case 5 -> patient.cancelAppointment(schedulingSystem);
            case 6 -> patient.viewScheduledAppointments(schedulingSystem);
            case 7 -> patient.viewPastAppointmentOutcomeRecords();
            case 8 -> {
                loggedIn = false;
                System.out.println("Logging out.");
            }
            default -> System.out.println("Invalid option. Please try again.");
        }
        return loggedIn;
    }

    // Handle Doctor options
    private boolean handleDoctorOptions(Doctor doctor, boolean loggedIn, int choice, Scanner sc) {
        switch (choice) {
            case 1 -> {
                doctor.viewAllPatients(patients);
                System.out.println("Enter patient ID to view medical record: ");
                String patientID = sc.next();
                Patient patient = findPatientByID(patientID);
                if (patient != null) {
                    doctor.viewPatientMedicalRecord(patient);
                } else {
                    System.out.println("Patient not found.");
                }
            }
            case 2 -> {
                doctor.viewAllPatients(patients);
                System.out.println("Enter patient ID to update medical record: ");
                String patientID = sc.next();
                Patient patient = findPatientByID(patientID);
                if (patient == null) {
                    System.out.println("Patient Not Found. Please Try Again.");
                } else {
                    doctor.updatePatientMedicalRecord(patient);
                }
            }
            case 3 -> doctor.viewPersonalSchedule();
            case 4 -> doctor.setAvailability();
            case 5 -> doctor.respondToAppointmentRequests(schedulingSystem);
            case 6 -> doctor.viewUpcomingAppointments();
            case 7 -> doctor.recordAppointmentOutcome(inventory);
            case 8 -> {
                loggedIn = false;
                System.out.println("Logging out.");
            }
            default -> System.out.println("Invalid option. Please try again.");
        }
        return loggedIn;
    }

    // Handle Pharmacist options
    private boolean handlePharmacistOptions(Pharmacist pharmacist, boolean loggedIn, int choice, Scanner sc) {
        switch (choice) {
            case 1 -> pharmacist.viewAppointmentOutcomeRecord(patients);
            case 2 -> pharmacist.updatePrescriptionStatus(patients, inventory);
            case 3 -> pharmacist.viewInventory(inventory);
            case 4 -> pharmacist.submitReplenishmentRequest(inventory);
            case 5 -> {
                loggedIn = false;
                System.out.println("Logging out.");
            }
            default -> System.out.println("Invalid option. Please try again.");
        }
        return loggedIn;
    }


    // Handle Administrator options
    private boolean handleAdminOptions(Administrator admin, boolean loggedIn, int choice, Scanner sc) {
        switch (choice) {
            case 1 -> admin.viewAndManageHospitalStaff(doctors, pharmacists, administrators, roles);
            case 2 -> admin.viewAllAppointments(schedulingSystem);
            case 3-> admin.viewAllUpcomingConfirmedAppointments(schedulingSystem);
            case 4->admin.viewPendingAppointments(schedulingSystem);
            case 5->admin.displayCompletedAppointments(schedulingSystem);    
            case 6 -> admin.viewAndManageMedicationInventory(inventory);
            case 7 -> admin.approveReplenishmentRequest(inventory);
            case 8 -> {
                loggedIn = false;
                System.out.println("Logging out.");
            }
            default -> System.out.println("Invalid option. Please try again.");
        }
        return loggedIn;
    }

    // Find Patient by ID
    private Patient findPatientByID(String patientID) {
        for (Patient patient : patients) {
            if (patient.getuserID().equals(patientID)) {
                return patient;
            }
        }
        return null;
    }

    // Getter for scheduling system
    public static SchedulingSystem getSchedulingSystem(){
        return schedulingSystem;
    }
}
