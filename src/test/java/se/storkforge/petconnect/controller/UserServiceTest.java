package se.storkforge.petconnect.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import entity.User;
import se.storkforge.petconnect.repository.UserRepository;
import se.storkforge.petconnect.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    public void testSaveUser() {
        User user = new User();
        user.setUsername("testuser");
        when(userRepository.save(user)).thenReturn(user);

        User savedUser = userService.save(user);

        assertNotNull(savedUser);
        assertEquals("testuser", savedUser.getUsername());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testFindAll() {
        // Arrange
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        List<User> users = List.of(user1, user2);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> foundUsers = (List<User>) userService.findAll();

        // Assert
        assertEquals(2, foundUsers.size());
        assertEquals("user1", foundUsers.get(0).getUsername());
        assertEquals("user2", foundUsers.get(1).getUsername());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testFindById() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        Optional<User> foundUserOptional = userService.findById(1L);

        // Assert
        assertTrue(foundUserOptional.isPresent()); // Check if the Optional contains a value
        User foundUser = foundUserOptional.get(); // Retrieve the User object from the Optional
        assertEquals("testuser", foundUser.getUsername()); // Now you can call getUsername()
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testDelete() {
        // Arrange
        Long userId = 1L;
        doNothing().when(userRepository).deleteById(userId);

        // Act
        userService.delete(userId);

        // Assert
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void testFindByUsername() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(user);

        // Act
        User foundUser = userService.findByUsername("testuser");

        // Assert
        assertNotNull(foundUser);
        assertEquals("testuser", foundUser.getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
    }
}
