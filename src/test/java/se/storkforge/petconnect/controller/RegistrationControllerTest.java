package se.storkforge.petconnect.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.ui.ConcurrentModel;
import org.springframework.validation.BindingResult;
import se.storkforge.petconnect.dto.RegistrationForm;
import se.storkforge.petconnect.entity.Role;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.repository.RoleRepository;
import se.storkforge.petconnect.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void processRegistration_ShouldRedirectOnSuccess() {
        // ARRANGE
        Role userRole = new Role("ROLE_USER");
        userRole.setId(1L);

        // Whenever userRepository.findByUsername("newuser") => return empty
        when(userRepository.findByUsername("newuser"))
                .thenReturn(Optional.empty());

        // Whenever roleRepository.findByName("ROLE_USER") => return userRole
        when(roleRepository.findByName("ROLE_USER"))
                .thenReturn(Optional.of(userRole));

        // Mock password encoding
        when(passwordEncoder.encode("Password123!"))
                .thenReturn("encodedPassword");

        // Create a registration form
        RegistrationForm form = new RegistrationForm(
                "newuser",                // username
                "newuser@example.com",            // email
                "Password123!",                  // password
                "Password123!"                  // confirmPassword
        );

        // We also need a mock BindingResult
        BindingResult mockBindingResult = mock(BindingResult.class);

        // ACT: call the controller method directly
        String viewName = registrationController.processRegistration(
                form,
                mockBindingResult,
                new ConcurrentModel() // or mock(Model.class)
        );

        // ASSERT: confirm it redirects
        assertEquals("redirect:/login?registered", viewName);

        // confirm that user is saved
        verify(userRepository).save(any(User.class));
    }

    @Test
    void processRegistration_ShouldThrowIfRoleMissing() {
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty()); // simulate missing role

        RegistrationForm form = new RegistrationForm(
                "newuser", "email@example.com", "Password123!", "Password123!"
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
                "existinguser", "email@example.com", "Password123!", "Password123!"
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
                "newuser", "email@example.com", "Password123!", "WrongConfirm!"
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
                "Pass1234"        // valid confirm
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
                "short"
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