package se.storkforge.petconnect.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import se.storkforge.petconnect.dto.RegistrationForm;
import se.storkforge.petconnect.entity.Role;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.repository.RoleRepository;
import se.storkforge.petconnect.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class RegistrationControllerUnitTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegistrationController registrationController;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void processRegistration_ShouldShowSuccessMessageAndStayOnRegisterPage() {
        // ARRANGE
        Role userRole = new Role("ROLE_USER");
        userRole.setId(1L);

        when(userRepository.findByUsername("newuser"))
                .thenReturn(Optional.empty());

        when(roleRepository.findByName("ROLE_USER"))
                .thenReturn(Optional.of(userRole));

        when(passwordEncoder.encode("Password123!"))
                .thenReturn("encodedPassword");

        RegistrationForm form = new RegistrationForm(
                "newuser",
                "newuser@example.com",
                "Password123!",
                "Password123!",
                "+46701234567"
        );

        BindingResult mockBindingResult = mock(BindingResult.class);
        when(mockBindingResult.hasErrors()).thenReturn(false);

        Model model = new ConcurrentModel();

        // ACT
        String viewName = registrationController.processRegistration(form, mockBindingResult, model);

        // ASSERT
        assertEquals("auth/register", viewName);
        assertTrue(model.containsAttribute("successMessage"));
        verify(userRepository).save(any(User.class));
    }


    @Test
    void processRegistration_ShouldThrowIfRoleMissing() {
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty()); // simulate missing role

        RegistrationForm form = new RegistrationForm(
                "newuser", "email@example.com", "Password123!", "Password123!","+46701111222"
        );
        BindingResult mockBindingResult = mock(BindingResult.class);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                registrationController.processRegistration(
                        form, mockBindingResult, new ConcurrentModel()
                )
        );

        assertEquals("Default role not found", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void processRegistration_ShouldReturnFormIfUsernameExists() {
        when(userRepository.findByUsername("existinguser"))
                .thenReturn(Optional.of(new User())); // Simulate existing user

        RegistrationForm form = new RegistrationForm(
                "existinguser", "email@example.com", "Password123!", "Password123!", "+46701111222"
        );
        BindingResult mockBindingResult = mock(BindingResult.class);

        String viewName = registrationController.processRegistration(
                form, mockBindingResult, new ConcurrentModel()
        );

        assertEquals("auth/register", viewName);
        verify(userRepository, never()).save(any());
    }


    @Test
    void processRegistration_ShouldReturnFormIfPasswordsDoNotMatch() {
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());

        RegistrationForm form = new RegistrationForm(
                "newuser", "email@example.com", "Password123!", "WrongConfirm!","+46701111222"

        );
        BindingResult mockBindingResult = mock(BindingResult.class);

        String viewName = registrationController.processRegistration(
                form, mockBindingResult, new ConcurrentModel()
        );

        assertEquals("auth/register", viewName);
        verify(userRepository, never()).save(any());
    }


    @Test
    void processRegistration_ShouldReturnRegisterIfValidationFails() {
        // pass an obviously invalid email to trigger validation constraints
        RegistrationForm form = new RegistrationForm(
                "Alice",
                "bad-email",      // invalid format
                "Pass1234",       // valid password
                "Pass1234",        // valid confirm
                "+46701111222"
        );

        // We simulate that "bad-email" triggered a validation error.
        BindingResult mockBindingResult = mock(BindingResult.class);
        when(mockBindingResult.hasErrors()).thenReturn(true);

        // ACT:
        String viewName = registrationController.processRegistration(
                form,
                mockBindingResult,
                new ConcurrentModel()
        );


        // Because the BindingResult has errors, we expect the controller
        // to stay on the "auth/register" page instead of redirecting
        assertEquals("auth/register", viewName);

        // Abd we never attempt to save a user, because of the validation errors
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void processRegistration_ShouldStayOnFormIfPasswordTooShort() {
        RegistrationForm form = new RegistrationForm(
                "Bob",
                "bob@example.com",
                "short",    // too short
                "short",
                "+46701111222"
        );

        BindingResult mockBindingResult = mock(BindingResult.class);
        when(mockBindingResult.hasErrors()).thenReturn(true);

        String viewName = registrationController.processRegistration(
                form,
                mockBindingResult,
                new ConcurrentModel()
        );

        assertEquals("auth/register", viewName);
        verify(userRepository, never()).save(any(User.class));
    }
}