package se.storkforge.petconnect.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.exception.UserNotFoundException;
import se.storkforge.petconnect.service.UserService;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerIntegrationTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new ExceptionHandlerController())
                .build();

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setPhoneNumber("+46762373333");
    }

    @Test
    void testGetAllUsersEndpoint() throws Exception {
        // Setup
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");

        List<User> users = Arrays.asList(testUser, user2);
        when(userService.getAllUsers()).thenReturn(users);

        // Test & Verify
        mockMvc.perform(get("/api/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("testuser")))
                .andExpect(jsonPath("$[1].username", is("user2")));
    }

    @Test
    void testGetUserByIdEndpoint() throws Exception {
        when(userService.getUserById(1L)).thenReturn(testUser);

        mockMvc.perform(get("/api/users/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")));
    }

    @Test
    void testGetUserByIdNotFound() throws Exception {
        when(userService.getUserById(1L)).thenThrow(new UserNotFoundException("Not found"));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateUserEndpoint() throws Exception {
        when(userService.createUser(any(User.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username", is("testuser")));
    }

    @Test
    void testUpdateUserEndpoint() throws Exception {
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("updated");
        updatedUser.setEmail("updated@example.com");

        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("updated")));
    }

    @Test
    void testUpdateUserNotFound() throws Exception {
        when(userService.updateUser(eq(99L), any(User.class)))
                .thenThrow(new UserNotFoundException("Not found"));

        mockMvc.perform(put("/api/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteUserEndpoint() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteUserNotFound() throws Exception {
        doThrow(new UserNotFoundException("Not found"))
                .when(userService).deleteUser(99L);

        mockMvc.perform(delete("/api/users/99"))
                .andExpect(status().isNotFound());
    }
    @Test
    void testCreateDuplicateUser() throws Exception {
        String errorMessage = "Username already exists";
        when(userService.createUser(any(User.class)))
                .thenThrow(new IllegalArgumentException(errorMessage));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));
    }

    @Test
    void testCreateDuplicateEmail() throws Exception {
        String errorMessage = "Email already exists.";
        User userWithDuplicateEmail = new User();
        userWithDuplicateEmail.setUsername("newuser");
        userWithDuplicateEmail.setEmail(testUser.getEmail()); // Use the existing test user's email
        userWithDuplicateEmail.setPassword("someSecurePassword");// Add a valid password
        userWithDuplicateEmail.setPhoneNumber("+46762373333");
        userWithDuplicateEmail.setId(null); // Explicitly set ID to null

        when(userService.createUser(any(User.class)))
                .thenThrow(new IllegalArgumentException(errorMessage));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userWithDuplicateEmail))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));
    }
}
