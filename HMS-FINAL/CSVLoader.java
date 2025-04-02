import java.io.*;
import java.util.List;

public class CSVLoader {

    // Save users to a file
    public static void saveUsers(List<User> users, String filePath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(users);
            System.out.println("Users saved successfully.");
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    // Load users from a file
    public static List<User> loadUsers(String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (List<User>) ois.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("User data file not found. Starting with default users.");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
        return null;
    }

    // Load patients from a CSV file
    public static void loadPatientsFromCSV(String filePath, Main mainInstance) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean header = true;

            while ((line = br.readLine()) != null) {
                if (header) {
                    header = false; // Skip header line
                    continue;
                }

                String[] values = line.split(",");
                if (values.length < 6) {
                    System.out.println("Invalid patient record. Skipping: " + line);
                    continue;
                }

                String patientID = values[0].trim();
                String name = values[1].trim();
                String dateOfBirth = values[2].trim();
                String gender = values[3].trim();
                String bloodType = values[4].trim();
                String email = values[5].trim();

                // Create a new Patient object and add it to Main's patients list
                Patient patient = new Patient(patientID, name, gender, dateOfBirth, email, bloodType);
                mainInstance.patients.add(patient);
                mainInstance.getSchedulingSystem().addPatient(patient);
            }
        }
    }

    // Load staff from a CSV file
    public static void loadStaffFromCSV(String filePath, Main mainInstance) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean header = true;

            while ((line = br.readLine()) != null) {
                if (header) {
                    header = false; // Skip header line
                    continue;
                }

                String[] values = line.split(",");
                //if (values.length < 5) {
                    //System.out.println("Invalid staff record. Skipping: " + line);
                    //continue;
                //}

                String staffID = values[0].trim();
                String name = values[1].trim();
                String role = values[2].trim();
                String gender = values[3].trim();
                

                try {
                    int age = Integer.parseInt(values[4].trim());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid age for staff record. Skipping: " + line);
                    continue;
                }

                switch (role) {
                    case "Doctor" -> {
                        if (values.length < 6) {
                            System.out.println("Invalid doctor record. Skipping: " + line);
                            continue;
                        }
                        String specialty = values[5].trim();
                        Doctor doctor = new Doctor(staffID, name, gender, specialty);
                        mainInstance.doctors.add(doctor);
                        mainInstance.getSchedulingSystem().addDoctor(doctor);
                    }
                    case "Pharmacist" -> {
                        Pharmacist pharmacist = new Pharmacist(staffID, name, gender);
                        mainInstance.pharmacists.add(pharmacist);
                    }
                    case "Administrator" -> {
                        Administrator admin = new Administrator(staffID, name, gender);
                        mainInstance.administrators.add(admin);
                    }
                    default -> System.out.println("Unknown role: " + role + ". Skipping: " + line);
                }
            }
            
        }
    }

    // Load medicines from a CSV file
    public static void loadMedicineFromCSV(String filePath, Main mainInstance) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean header = true;

            while ((line = br.readLine()) != null) {
                if (header) {
                    header = false; // Skip header line
                    continue;
                }

                String[] values = line.split(",");
                if (values.length < 4) {
                    System.out.println("Invalid medicine record. Skipping: " + line);
                    continue;
                }

                String medicationID = values[0].trim();
                String name = values[1].trim();
                int quantity;
                int lowStockAlert;

                try {
                    quantity = Integer.parseInt(values[2].trim());
                    lowStockAlert = Integer.parseInt(values[3].trim());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid numeric fields in medicine record. Skipping: " + line);
                    continue;
                }

                Medication medication = new Medication(medicationID, name, quantity, lowStockAlert);
                mainInstance.inventory.updateInventory(medication);
            }
        }
    }
}
