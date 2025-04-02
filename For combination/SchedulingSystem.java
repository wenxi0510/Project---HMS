import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class SchedulingSystem {
    private Map<Doctor, Map<LocalDateTime, Boolean>> doctorAvailability;
    private List<Appointment> appointments;
    private List<Appointment> pendingAppointments;
    private List<Patient> patients; // Centralized patient list

    public SchedulingSystem() {
        doctorAvailability = new HashMap<>();
        appointments = new ArrayList<>();
        pendingAppointments = new ArrayList<>();
        patients = new ArrayList<>();
    }

    public void addDoctor(Doctor doctor) {
        doctorAvailability.put(doctor, new HashMap<>());
        updateDoctorAvailability(doctor);
    }

    public void addPatient(Patient patient) {
        patients.add(patient);
    }

    public void updateDoctorAvailability(Doctor doctor) {
        Map<LocalDateTime, Boolean> availabilityMap = new HashMap<>();
        doctor.getAvailability().forEach((date, timeRanges) -> {
            for (TimeRange range : timeRanges) {
                LocalDateTime start = date.atTime(range.getStart());
                LocalDateTime end = date.atTime(range.getEnd());
                while (!start.isAfter(end.minusMinutes(30))) {
                    availabilityMap.put(start, true);
                    start = start.plusMinutes(30);
                }
            }
        });
        doctorAvailability.put(doctor, availabilityMap);
    }

    public void addPendingAppointment(Appointment appointment) {
        pendingAppointments.add(appointment);
    }

    public Appointment getPendingAppointmentById(String appointmentID) {
        for (Appointment appointment : pendingAppointments) {
            if (appointment.getAppointmentID().equals(appointmentID)) {
                return appointment;
            }
        }
        return null;
    }

    public void viewScheduledAppointmentsForPatient(Patient patient) {
        System.out.println("\nAll Appointments for Patient: " + patient.getName() + ", Patient ID: " + patient.getuserID());
    
        // List to store all appointments for the patient
        List<Appointment> patientAppointments = new ArrayList<>();
    
        // Add confirmed and cancelled appointments from the central appointments list
        for (Appointment appointment : appointments) {
            if (appointment.getPatientID().equals(patient.getuserID())) {
                patientAppointments.add(appointment);
            }
        }
    
        // Add pending appointments from the pendingAppointments list
        for (Appointment appointment : pendingAppointments) {
            if (appointment.getPatientID().equals(patient.getuserID())) {
                patientAppointments.add(appointment);
            }
        }
    
        // Sort appointments by date and time
        patientAppointments.sort(Comparator.comparing(Appointment::getDateTime));
    
        if (patientAppointments.isEmpty()) {
            System.out.println("This patient has no appointments.");
        } else {
            System.out.println("--- Appointments ---");
            for (Appointment appointment : patientAppointments) {
                Doctor doctor = getDoctorById(appointment.getDoctorID());
                System.out.printf(
                    "Date: %s, Time: %s, Doctor: Dr. %s, Appointment ID: %s, Status: %s%n",
                    appointment.getDateTime().toLocalDate(),
                    appointment.getDateTime().toLocalTime(),
                    doctor != null ? doctor.getName() : "Unknown",
                    appointment.getAppointmentID(),
                    appointment.getStatus()
                );
            }
            System.out.println("--------------------");
        }
    }
    


    public void viewPendingAppointmentsForDoctor(Doctor doctor) {
        System.out.println("\nPending Appointments for Dr. " + doctor.getName() + ", Doctor ID: " + doctor.getDoctorID());
    
        // Filter and format pending appointments
        List<Appointment> doctorPendingAppointments = new ArrayList<>();
        for (Appointment appointment : pendingAppointments) {
            if (appointment.getDoctorID().equals(doctor.getDoctorID())) {
                doctorPendingAppointments.add(appointment);
            }
        }
    
        // Sort appointments by date and time
        doctorPendingAppointments.sort(Comparator.comparing(Appointment::getDateTime));
    
        if (doctorPendingAppointments.isEmpty()) {
            System.out.println("No pending appointment requests found.");
        } else {
            System.out.println("--- Pending Appointments ---");
            for (Appointment appointment : doctorPendingAppointments) {
                System.out.printf(
                    "Date: %s, Time: %s, Patient: %s, Appointment ID: %s%n",
                    appointment.getDateTime().toLocalDate(),
                    appointment.getDateTime().toLocalTime(),
                    appointment.getPatientID(),
                    appointment.getAppointmentID()
                );
            }
            System.out.println("----------------------------");
        }
    }

    public List<Appointment> getPendingAppointmentsForDoctor(Doctor doctor) {
        List<Appointment> doctorPendingAppointments = new ArrayList<>();
        for (Appointment appointment : pendingAppointments) {
            if (appointment.getDoctorID().equals(doctor.getDoctorID())) {
                doctorPendingAppointments.add(appointment);
            }
        }
        return doctorPendingAppointments;
    }

    public void respondToPendingAppointment(Appointment appointment, boolean isAccepted) {
        if (pendingAppointments.contains(appointment)) {
            pendingAppointments.remove(appointment);
            if (isAccepted) {
                confirmAppointment(appointment);
                System.out.println("Appointment confirmed.");
            } else {
                System.out.println("Appointment rejected.");
            }
        } else {
            System.out.println("Appointment not found in pending list.");
        }
    }

    private static int appointmentCounter = 1; // Centralized ID generator

    private String generateAppointmentID() {
        return "A" + (appointmentCounter++);
    }

    public Appointment bookSlot(Patient patient, Doctor doctor, LocalDateTime slot, String comments) {
        LocalDateTime now = LocalDateTime.now();
        int bookingDaysRange = 30; // Change this to allow bookings for the next 'n' days
        LocalDateTime bookingEnd = now.plusDays(bookingDaysRange).with(LocalTime.of(18, 0));
    
        // Check if the slot is in the past
        if (slot.isBefore(now)) {
            System.out.println("Invalid slot. You cannot book appointments for past dates or times.");
            return null;
        }
    
        // Check if the slot exceeds the booking period
        if (slot.isAfter(bookingEnd)) {
            System.out.println("Invalid slot. You can only book appointments within the next " + bookingDaysRange + " days.");
            return null;
        }
    
        // Check if the slot falls outside working hours
        if (slot.toLocalTime().isBefore(LocalTime.of(9, 0)) || 
        slot.toLocalTime().isAfter(LocalTime.of(17, 30)) || 
        (slot.toLocalTime().isAfter(LocalTime.of(11, 59)) && slot.toLocalTime().isBefore(LocalTime.of(13, 0)))) {
        System.out.println("Invalid slot. Bookings are only allowed between 09:00–12:00 and 13:00–17:30.");
        return null;
    }
    
    
    // Check for conflicts in the doctor's schedule
    LocalDateTime slotEnd = slot.plusMinutes(30); // Assuming 30-minute slots
    if (doctor.hasConflict(slot, slotEnd)) {
        System.out.println("Conflict detected. Unable to book the slot: " + slot);
        return null;
    }
    
    // Check availability in the centralized scheduling system
    Map<LocalDateTime, Boolean> availability = doctorAvailability.get(doctor);
        if (availability != null && availability.getOrDefault(slot, true)) {
            // Generate unique appointment ID
            String appointmentID = generateAppointmentID();
            System.out.println("Generated Appointment ID: " + appointmentID);
    
            // Create the new appointment
            Appointment newAppointment = new Appointment(appointmentID, patient.getuserID(), doctor.getDoctorID(), slot, comments);
            addPendingAppointment(newAppointment); // Add to pending appointments
    
            System.out.println("Appointment request sent to Dr. " + doctor.getName() + " for approval.");
            return newAppointment;
        }
    
        System.out.println("The selected slot is unavailable. Please choose another slot.");
        return null;
    }
    
    
    public void confirmAppointment(Appointment appointment) {
        Doctor doctor = getDoctorById(appointment.getDoctorID());
        Patient patient = getPatientById(appointment.getPatientID());

        // if (doctor == null || patient == null) {
        //    System.out.println("Doctor or patient not found. Cannot confirm appointment.");
        //    return;
        // }

        appointment.confirm();
        appointments.add(appointment);
        doctor.addAppointment(appointment);
        patient.getAppointments().add(appointment);

        Map<LocalDateTime, Boolean> availability = doctorAvailability.get(doctor);
        if (availability != null) {
            availability.put(appointment.getDateTime(), false);
        }
    }

    public Patient getPatientById(String patientID) {
        for (Patient patient : patients) {
            if (patient.getuserID().equals(patientID)) {
                return patient;
            }
        }
        return null;
    }

    public Doctor getDoctorById(String doctorID) {
        for (Doctor doctor : doctorAvailability.keySet()) {
            if (doctor.getDoctorID().equals(doctorID)) {
                return doctor;
            }
        }
        return null;
    }


    // SchedulingSystem: Check if a slot is available for a specific doctor
    public boolean isSlotAvailable(Doctor doctor, LocalDateTime slot) {
        if (doctorAvailability == null) {
            System.out.println("Doctor availability is not initialized.");
            return false;
        }

        Map<LocalDateTime, Boolean> availability = doctorAvailability.get(doctor);
        if (availability == null) {
            System.out.println("No availability information found for Dr. " + doctor.getName());
            return false;
        }

        return availability.getOrDefault(slot, false);
    }




    public boolean displayAvailableSlotsForDoctor(Doctor doctor) {
        System.out.println("""
            Choose a time range for available appointments:
            1: This week's available appointments
            2: This month's available appointments (next 30 days)
            """);

        int choice = getIntInput(); // Helper method to handle input validation
        LocalDateTime now = LocalDateTime.now(); // Current date and time
        LocalDate endDate; // Define the end date based on the choice

        switch (choice) {
            case 1 -> endDate = now.toLocalDate().plusDays(7); // End of the current week
            case 2 -> endDate = now.toLocalDate().plusDays(30); // Next 30 days
            default -> {
                System.out.println("Invalid choice. Returning to the main menu.");
                return false;
            }
        }

        Map<LocalDateTime, Boolean> availability = doctorAvailability.get(doctor);
        if (availability == null || availability.isEmpty()) {
            System.out.println("No available slots for Dr. " + doctor.getName());
            return false;
        }

        boolean hasSlots = false;
        Map<LocalDate, List<LocalTime>> formattedAvailability = new TreeMap<>();

        // Organize slots by date for cleaner display
        for (Map.Entry<LocalDateTime, Boolean> entry : availability.entrySet()) {
            LocalDateTime slot = entry.getKey();
            if (entry.getValue() && slot.isAfter(now) && slot.toLocalDate().isBefore(endDate.plusDays(1))) {
                hasSlots = true;
                LocalDate date = slot.toLocalDate();
                LocalTime time = slot.toLocalTime();
                formattedAvailability.computeIfAbsent(date, k -> new ArrayList<>()).add(time);
            }
        }

        if (!hasSlots) {
            System.out.println("No available slots for Dr. " + doctor.getName());
            return false;
        }

        // Sort the times for each date
        for (List<LocalTime> times : formattedAvailability.values()) {
            times.sort(Comparator.naturalOrder());
        }

        // Display formatted availability
        System.out.println("Available slots for Dr. " + doctor.getName() + ":");
        for (Map.Entry<LocalDate, List<LocalTime>> entry : formattedAvailability.entrySet()) {
            System.out.println("Date: " + entry.getKey());
            System.out.print("  Times: ");
            for (LocalTime time : entry.getValue()) {
                System.out.print(time + " ");
            }
            System.out.println(); // New line after each date
        }

        return true; // Slots are available
    }

    // Declare Scanner globally at the top of your class
    private static Scanner sc = new Scanner(System.in);

    // Helper method for input validation
    private int getIntInput() {
        int input = -1; // Default to an invalid number
        boolean valid = false;

        while (!valid) {
            System.out.print("Enter your choice: ");
            if (sc.hasNextInt()) {
                input = sc.nextInt();
                sc.nextLine(); // Clear the buffer
                valid = true; // Valid integer input
            } else {
                System.out.println("Invalid input. Please enter a valid number.");
                sc.next(); // Clear the invalid input
            }
        }
        return input;
    }



    // Get doctors by specialty
    public List<Doctor> getDoctorsBySpecialty(String specialty) {
        List<Doctor> doctorsBySpecialty = new ArrayList<>();
        for (Doctor doctor : doctorAvailability.keySet()) {
            if (specialty.equalsIgnoreCase(doctor.getSpecialty())) {
                doctorsBySpecialty.add(doctor);
            }
        }
        return doctorsBySpecialty;
    }

    public void cancelAppointment(Appointment appointment) {
    if (appointment == null) {
        System.out.println("No appointment provided for cancellation.");
        return;
    }

    // Update availability for the doctor
    Doctor doctor = this.getDoctorById(appointment.getDoctorID());
    if (doctor != null) {
        Map<LocalDateTime, Boolean> availability = this.doctorAvailability.get(doctor);
        if (availability != null) {
            availability.put(appointment.getDateTime(), true); // Mark the slot as available
        }
        doctor.cancelAppointment(appointment); // Notify the doctor
    }

    // Notify the patient
    Patient patient = this.getPatientById(appointment.getPatientID());
    if (patient != null) {
        System.out.println("Notifying patient of cancellation...");
        // Optionally, add notification logic here
    }

    // Update the appointment status to "Cancelled"
    appointment.cancel();
    System.out.println("Appointment status updated to 'Cancelled'.");
}



    public Appointment rescheduleAppointment(Appointment oldAppointment, Doctor newDoctor, LocalDateTime newSlot, String comments) {
        if (oldAppointment == null) {
            System.out.println("No appointment provided for rescheduling.");
            return null;
        }
    
        cancelAppointment(oldAppointment); // Cancel the old appointment
    
        Patient patient = getPatientById(oldAppointment.getPatientID());
        if (patient == null) {
            System.out.println("Patient not found for rescheduling.");
            return null;
        }
    
        // Book a new appointment
        return bookSlot(patient, newDoctor, newSlot, comments);
    }
    


    // Check if a patient has a previous appointment
    public boolean hasPreviousAppointment(Patient patient) {
        for (Appointment appointment : appointments) {
            if (appointment.getPatientID().equals(patient.getuserID())) {
                return true;
            }
        }
        return false;
    }

    // Get the last appointment for a patient
    public Appointment getLastAppointment(Patient patient) {
        List<Appointment> patientAppointments = new ArrayList<>();
        for (Appointment appointment : appointments) {
            if (appointment.getPatientID().equals(patient.getuserID())) {
                patientAppointments.add(appointment);
            }
        }

        // Sort appointments by date and return the most recent one
        patientAppointments.sort(Comparator.comparing(Appointment::getDateTime).reversed());
        return patientAppointments.isEmpty() ? null : patientAppointments.get(0);
    }

    // FOR ADMINISTRATOR
    public void displayUpcomingConfirmedAppointments() {
        System.out.println("\nUpcoming Confirmed Appointments:");
    
        // Filter and sort confirmed appointments
        List<Appointment> confirmedAppointments = appointments.stream()
            .filter(appointment -> "Confirmed".equalsIgnoreCase(appointment.getStatus()) &&
                                    appointment.getDateTime().isAfter(LocalDateTime.now()))
            .sorted(Comparator.comparing(Appointment::getDateTime))
            .toList();
    
        if (confirmedAppointments.isEmpty()) {
            System.out.println("No upcoming confirmed appointments.");
            return;
        }
    
        // Display formatted output
        for (Appointment appointment : confirmedAppointments) {
            Doctor doctor = getDoctorById(appointment.getDoctorID());
            Patient patient = getPatientById(appointment.getPatientID());
    
            System.out.printf("Date & Time: %s | Appointment ID: %s | Doctor: Dr. %s | Patient: %s%n",
                appointment.getDateTime().toLocalDate() + " " + appointment.getDateTime().toLocalTime(),
                appointment.getAppointmentID(),
                doctor != null ? doctor.getName() : "Unknown",
                patient != null ? patient.getName() : "Unknown");
        }
    }

public void displayAllAppointments() {
    System.out.println("\nAll Appointments (Pending, Confirmed, and Completed):");

    // Combine all appointments (pending, confirmed, and completed) into a single list
    List<Appointment> allAppointments = new ArrayList<>();
    allAppointments.addAll(appointments); // Confirmed and completed appointments
    allAppointments.addAll(pendingAppointments); // Pending appointments

    // Sort appointments by date and time
    allAppointments.sort(Comparator.comparing(Appointment::getDateTime));

    if (allAppointments.isEmpty()) {
        System.out.println("No appointments found.");
        return;
    }

    // Display formatted output
    for (Appointment appointment : allAppointments) {
        Doctor doctor = getDoctorById(appointment.getDoctorID());
        Patient patient = getPatientById(appointment.getPatientID());

        System.out.printf(
            "Date & Time: %s | Appointment ID: %s | Status: %s | Doctor: Dr. %s | Patient: %s%n",
            appointment.getDateTime().toLocalDate() + " " + appointment.getDateTime().toLocalTime(),
            appointment.getAppointmentID(),
            appointment.getStatus(),
            doctor != null ? doctor.getName() : "Unknown",
            patient != null ? patient.getName() : "Unknown"
        );

        // If the appointment is completed, display prescribed medications
        if ("Completed".equalsIgnoreCase(appointment.getStatus())) {
            if (appointment.getPrescribedMedication() != null && !appointment.getPrescribedMedication().isEmpty()) {
                System.out.println("  Prescribed Medications:");
                for (Map.Entry<Medication, Integer> entry : appointment.getPrescribedMedication().entrySet()) {
                    System.out.printf("    - Medication: %s | Quantity: %d%n",
                        entry.getKey().getName(),
                        entry.getValue());
                }
            } else {
                System.out.println("  No medications prescribed.");
            }
        }
    }

    System.out.println("---------------------------");
}

    
    public void displayPendingAppointments() {
        System.out.println("All Appointments Pending Doctor Approval:");
    
        // Sort pending appointments by date and time
        List<Appointment> sortedPendingAppointments = pendingAppointments.stream()
            .sorted(Comparator.comparing(Appointment::getDateTime))
            .toList();
    
        if (sortedPendingAppointments.isEmpty()) {
            System.out.println("No pending appointments.");
            return;
        }
    
        // Display formatted output
        for (Appointment appointment : sortedPendingAppointments) {
            Doctor doctor = getDoctorById(appointment.getDoctorID());
            Patient patient = getPatientById(appointment.getPatientID());
    
            System.out.printf("Date & Time: %s | Appointment ID: %s | Doctor: Dr. %s | Patient: %s%n",
                appointment.getDateTime().toLocalDate() + " " + appointment.getDateTime().toLocalTime(),
                appointment.getAppointmentID(),
                doctor != null ? doctor.getName() : "Unknown",
                patient != null ? patient.getName() : "Unknown");
        }
    }

    public void displayCompletedAppointments() {
    System.out.println("\nCompleted Appointments:");

    // Filter completed appointments
    List<Appointment> completedAppointments = appointments.stream()
        .filter(appointment -> "Completed".equalsIgnoreCase(appointment.getStatus()))
        .sorted(Comparator.comparing(Appointment::getDateTime))
        .toList();

    if (completedAppointments.isEmpty()) {
        System.out.println("No completed appointments found.");
        return;
    }

    // Display formatted details
    for (Appointment appointment : completedAppointments) {
        System.out.printf(
            "Appointment ID: %s | Date: %s | Time: %s | Patient ID: %s | Doctor ID: %s%n",
            appointment.getAppointmentID(),
            appointment.getDateTime().toLocalDate(),
            appointment.getDateTime().toLocalTime(),
            appointment.getPatientID(),
            appointment.getDoctorID()
        );

        // Display prescribed medications
        if (appointment.getPrescribedMedication() != null && !appointment.getPrescribedMedication().isEmpty()) {
            System.out.println("  Prescribed Medications:");
            for (Map.Entry<Medication, Integer> entry : appointment.getPrescribedMedication().entrySet()) {
                System.out.printf("    - Medication: %s | Quantity: %d%n", 
                    entry.getKey().getName(), 
                    entry.getValue());
            }
        } else {
            System.out.println("  No medications prescribed.");
        }
    }

    System.out.println("---------------------------");
}

}
