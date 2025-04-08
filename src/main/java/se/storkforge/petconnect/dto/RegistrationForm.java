package se.storkforge.petconnect.dto;

import jakarta.validation.constraints.*;

public class RegistrationForm {

    @NotBlank(message = "Username is required")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_-]{2,29}$", message = "Username must start with a letter and contain only letters, numbers, underscores or hyphens (7–30 chars)")
    private String username;

    @Email(message = "Invalid Email Format")
    @Size(max = 254)
    @Pattern(regexp = "^[\\w.-]+@[\\w.-]+\\.(com|net|org|dev|app|ai)$", message = "Email must end with .com, .net, or .org")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long.")
    private String password;

    @NotEmpty(message = "You need to confirm your password")
    private String confirmPassword;

    public RegistrationForm() {
    }

    public RegistrationForm(String username, String email, String password, String confirmPassword) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }


}
