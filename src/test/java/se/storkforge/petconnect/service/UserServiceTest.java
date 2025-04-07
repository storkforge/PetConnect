package se.storkforge.petconnect.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.exception.UserNotFoundException;
import se.storkforge.petconnect.repository.UserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User createUser(String username, String email, String password) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }

    private User createUserWithId(Long id, String username, String email, String password) {
        User user = createUser(username, email, password);
        user.setId(id);
        return user;
    }

    // CREATE USER TEST
    @Test
    void createUser_validUser_shouldSaveUser() {
        // Arrange
        User userToCreate = createUser("testuser", "test@example.com", "password");
        when(userRepository.findByUsername(userToCreate.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(userToCreate.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(userToCreate.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(userToCreate)).thenReturn(userToCreate);

        // Act
        User createdUser = userService.createUser(userToCreate);

        // Assert
        assertNotNull(createdUser);
        assertEquals("encodedPassword", createdUser.getPassword());
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(userToCreate);
    }

    @Test
    void createUser_usernameExists_shouldThrowException() {
        User user = createUser("existing", "test@test.com", "pass");
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(new User()));

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(user));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_emailExists_shouldThrowException() {
        User user = createUser("newuser", "existing@test.com", "pass");
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(new User()));

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(user));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_invalidEmail_shouldThrowException() {
        User user = createUser("newuser", "invalid-email", "pass");
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(user));
        verify(userRepository, never()).save(any());
    }

    // GET USER TESTS
    @Test
    void getUserById_existingUser_shouldReturnUser() {
        User expected = createUserWithId(1L, "test", "test@test.com", "pass");
        when(userRepository.findById(1L)).thenReturn(Optional.of(expected));

        User result = userService.getUserById(1L);
        assertEquals(expected, result);
    }

    @Test
    void getUserById_nonExisting_shouldThrowException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(99L));
    }

    // UPDATE USER TESTS  
    @Test
    void updateUser_validUpdate_shouldSaveChanges() {
        User existing = createUserWithId(1L, "old", "old@test.com", "oldpass");
        User update = createUserWithId(1L, "new", "new@test.com", "newpass");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("newpass")).thenReturn("encodedNewPass");
        when(userRepository.save(any(User.class))).thenReturn(update);

        User result = userService.updateUser(1L, update);

        assertEquals("new", result.getUsername());
        assertEquals("new@test.com", result.getEmail());
        verify(passwordEncoder).encode("newpass");
    }

    @Test
    void updateUser_nonExisting_shouldThrowException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(99L, new User()));
    }

    // DELETE USER TESTS
    @Test
    void deleteUser_existingUser_shouldDelete() {
        when(userRepository.existsById(1L)).thenReturn(true);
        userService.deleteUser(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_nonExisting_shouldThrowException() {
        when(userRepository.existsById(99L)).thenReturn(false);
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(99L));
    }

    // OTHER TESTS
    @Test
    void getAllUsers_shouldReturnAllUsers() {
        List<User> users = Arrays.asList(
                createUserWithId(1L, "user1", "u1@test.com", "pass1"),
                createUserWithId(2L, "user2", "u2@test.com", "pass2")
        );
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();
        assertEquals(2, result.size());
    }

    @Test
    void existsById_existingUser_shouldReturnTrue() {
        when(userRepository.existsById(1L)).thenReturn(true);
        assertTrue(userService.existsById(1L));
    }

    @Test
    void existsById_nonExisting_shouldReturnFalse() {
        when(userRepository.existsById(99L)).thenReturn(false);
        assertFalse(userService.existsById(99L));
    }
}