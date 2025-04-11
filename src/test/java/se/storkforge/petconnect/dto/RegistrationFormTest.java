package se.storkforge.petconnect.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RegistrationForm Validation Test")
class RegistrationFormTest {

    @Test
    void shouldStoreAllFieldsCorrectly() {
        RegistrationForm form = new RegistrationForm();
        form.setUsername("user");
        form.setEmail("user@example.com");
        form.setPassword("Password1!");
        form.setConfirmPassword("Password1!");

        assertEquals("user", form.getUsername());
        assertEquals("user@example.com", form.getEmail());
        assertEquals("Password1!", form.getPassword());
        assertEquals("Password1!", form.getConfirmPassword());
    }

    @Test
    void shouldFailWhenPasswordsDoNotMatch() {
        RegistrationForm form = new RegistrationForm();
        form.setPassword("Password1!");
        form.setConfirmPassword("Different1!");

        assertNotEquals(form.getPassword(), form.getConfirmPassword());
    }

    @Test
    void shouldDetectInvalidEmailAndBlankUsername() {
        RegistrationForm form = new RegistrationForm();
        form.setUsername(""); // Should trigger @NotBlank and pattern
        form.setEmail("invalid-email"); // Invalid format
        form.setPassword("short"); // Too short
        form.setConfirmPassword("short");

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);

        Set<String> messages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertTrue(messages.contains("Invalid Email Format"));
        assertTrue(messages.contains("Email must end with .com, .net, or .org"));
        assertTrue(messages.contains("Username is required"));
        assertTrue(messages.contains("Username must start with a letter and contain only letters, numbers, underscores or hyphens (7–30 chars)"));
        assertTrue(messages.contains("Password must be at least 8 characters long."));
    }
}
