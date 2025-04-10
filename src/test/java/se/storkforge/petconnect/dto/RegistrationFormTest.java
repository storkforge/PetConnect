package se.storkforge.petconnect.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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
}
