import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String userID;
    protected String passwordHash; // Store hashed password
    protected String name;
    protected String gender;
    protected String role;
    protected boolean isFirstLogin;

    private static final String DEFAULT_PASSWORD = "password";

    public User(String userID, String password, String name, String gender, String role) {
        this.userID = userID;
        this.passwordHash = hashPassword(password != null ? password : DEFAULT_PASSWORD);
        this.name = name;
        this.gender = gender;
        this.role = role;
        this.isFirstLogin = password == null || password.equals(DEFAULT_PASSWORD);
    }

    public String getuserID(){
        return userID;
    }

    public String getPassword(){
        return passwordHash;
    }

    public String getName(){
        return name;
    }

    public String getGender(){
        return gender;
    }

    // Hash the password
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    // Check hashed password for login
    public boolean login(String userID, String password) {
        if (this.userID.equals(userID) && this.passwordHash.equals(hashPassword(password))) {
            if (isFirstLogin) {
                System.out.println("Welcome, " + name + "! Since this is your first login, please change your password.");
                if (changePassword()) {
                    Main.saveDataOnChange(); // Save data after password change
                }
                isFirstLogin = false;
            }
            return true;
        }
        return false;
    }

    // Change password method
    public boolean changePassword() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter new password: ");
        String newPassword = sc.nextLine();
        while (newPassword.equals("password") || newPassword.length() < 6) {
            System.out.println("Password must be different from 'password' and at least 6 characters long.");
            System.out.print("Enter new password: ");
            newPassword = sc.nextLine();
        }
        this.passwordHash = hashPassword(newPassword);
        System.out.println("Password successfully changed!");
        return true;  // Indicate password was changed
    }

    public abstract void displayMenu();
}

