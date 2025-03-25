package se.storkforge.petconnect.controller.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.exeption.UserNotFoundException;
import se.storkforge.petconnect.repository.UserRepository;
import se.storkforge.petconnect.service.UserService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_validUser_shouldSaveUser() {
        // Arrange
        User userToCreate = new User();
        userToCreate.setUsername("testuser");
        userToCreate.setEmail("test@example.com");
        // Assuming your User entity has a setPassword method
        userToCreate.setPassword("password");
        when(userRepository.findByUsername(userToCreate.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(userToCreate.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(userToCreate)).thenReturn(userToCreate);

        // Act
        User createdUser = userService.createUser(userToCreate);

        // Assert
        assertNotNull(createdUser);
        assertEquals(userToCreate.getUsername(), createdUser.getUsername());
        assertEquals(userToCreate.getEmail(), createdUser.getEmail());
        verify(userRepository, times(1)).findByUsername(userToCreate.getUsername());
        verify(userRepository, times(1)).findByEmail(userToCreate.getEmail());
        verify(userRepository, times(1)).save(userToCreate);
    }

    @Test
    void createUser_usernameExists_shouldThrowIllegalArgumentException() {
        // Arrange
        User userToCreate = new User();
        userToCreate.setUsername("existinguser");
        userToCreate.setEmail("new@example.com");
        userToCreate.setPassword("password");
        when(userRepository.findByUsername(userToCreate.getUsername())).thenReturn(Optional.of(new User()));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(userToCreate));
        verify(userRepository, times(1)).findByUsername(userToCreate.getUsername());
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_emailExists_shouldThrowIllegalArgumentException() {
        // Arrange
        User userToCreate = new User();
        userToCreate.setUsername("newuser");
        userToCreate.setEmail("existing@example.com");
        userToCreate.setPassword("password");
        when(userRepository.findByUsername(userToCreate.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(userToCreate.getEmail())).thenReturn(Optional.of(new User()));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(userToCreate));
        verify(userRepository, times(1)).findByUsername(userToCreate.getUsername());
        verify(userRepository, times(1)).findByEmail(userToCreate.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_existingId_shouldReturnUser() {
        // Arrange
        Long userId = 1L;
        User expectedUser = new User();
        expectedUser.setId(userId);
        expectedUser.setUsername("testuser");
        expectedUser.setEmail("test@example.com");
        expectedUser.setPassword("password");
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        // Act
        User retrievedUser = userService.getUserById(userId);

        // Assert
        assertNotNull(retrievedUser);
        assertEquals(userId, retrievedUser.getId());
        assertEquals(expectedUser.getUsername(), retrievedUser.getUsername());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getUserById_nonExistingId_shouldThrowUserNotFoundException() {
        // Arrange
        Long userId = 100L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(userId));
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getUserByUsername_existingUsername_shouldReturnOptionalUser() {
        // Arrange
        String username = "testuser";
        User expectedUser = new User();
        expectedUser.setUsername(username);
        expectedUser.setEmail("test@example.com");
        expectedUser.setPassword("password");
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(expectedUser));

        // Act
        Optional<User> retrievedUserOptional = userService.getUserByUsername(username);

        // Assert
        assertTrue(retrievedUserOptional.isPresent());
        assertEquals(username, retrievedUserOptional.get().getUsername());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void getUserByUsername_nonExistingUsername_shouldReturnEmptyOptional() {
        // Arrange
        String username = "nonexistentuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act
        Optional<User> retrievedUserOptional = userService.getUserByUsername(username);

        // Assert
        assertFalse(retrievedUserOptional.isPresent());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void getUserByEmail_existingEmail_shouldReturnOptionalUser() {
        // Arrange
        String email = "test@example.com";
        User expectedUser = new User();
        expectedUser.setUsername("testuser");
        expectedUser.setEmail(email);
        expectedUser.setPassword("password");
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(expectedUser));

        // Act
        Optional<User> retrievedUserOptional = userService.getUserByEmail(email);

        // Assert
        assertTrue(retrievedUserOptional.isPresent());
        assertEquals(email, retrievedUserOptional.get().getEmail());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void getUserByEmail_nonExistingEmail_shouldReturnEmptyOptional() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        Optional<User> retrievedUserOptional = userService.getUserByEmail(email);

        // Assert
        assertFalse(retrievedUserOptional.isPresent());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() {
        // Arrange
        List<User> expectedUsers = Arrays.asList(
                createUser("user1", "user1@example.com", "pass1"),
                createUser("user2", "user2@example.com", "pass2")
        );
        when(userRepository.findAll()).thenReturn(expectedUsers);

        // Act
        List<User> actualUsers = userService.getAllUsers();

        // Assert
        assertEquals(expectedUsers.size(), actualUsers.size());
        assertEquals(expectedUsers, actualUsers);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void updateUser_existingId_shouldUpdateUser() {
        // Arrange
        Long userId = 1L;
        User existingUser = createUser("olduser", "old@example.com", "oldpass");
        existingUser.setId(userId);
        User updatedUser = createUser("newuser", "new@example.com", "newpass");
        updatedUser.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        User result = userService.updateUser(userId, updatedUser);

        // Assert
        assertNotNull(result);
        assertEquals(updatedUser.getUsername(), result.getUsername());
        assertEquals(updatedUser.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_nonExistingId_shouldThrowUserNotFoundException() {
        // Arrange
        Long userId = 100L;
        User updatedUser = createUser("newuser", "new@example.com", "newpass");
        updatedUser.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.updateUser(userId, updatedUser));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_existingId_shouldDeleteUser() {
        // Arrange
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(userId);

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void deleteUser_nonExistingId_shouldThrowUserNotFoundException() {
        // Arrange
        Long userId = 100L;
        when(userRepository.existsById(userId)).thenReturn(false);

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(userId));
        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, never()).deleteById(userId);
    }

    @Test
    void existsById_existingId_shouldReturnTrue() {
        // Arrange
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);

        // Act
        boolean exists = userService.existsById(userId);

        // Assert
        assertTrue(exists);
        verify(userRepository, times(1)).existsById(userId);
    }

    @Test
    void existsById_nonExistingId_shouldReturnFalse() {
        // Arrange
        Long userId = 100L;
        when(userRepository.existsById(userId)).thenReturn(false);

        // Act
        boolean exists = userService.existsById(userId);

        // Assert
        assertFalse(exists);
        verify(userRepository, times(1)).existsById(userId);
    }

    private User createUser(String username, String email, String password) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        // Assuming your User entity has a setPassword method
        user.setPassword(password);
        return user;
    }

    // Add more test cases to cover edge cases, error scenarios, and more complex scenarios.
    // For example, testing the updateUser method with specific field updates.
}