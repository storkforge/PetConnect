package se.storkforge.petconnect.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.ui.Model;
import se.storkforge.petconnect.entity.MeetUp;
import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.entity.Role;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.service.PetService;
import se.storkforge.petconnect.service.UserService;

import java.security.Principal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private PetService petService;

    @Mock
    private Principal principal;

    @Mock
    private Model model;

    @InjectMocks
    private UserProfileController userProfileController;

    private User testUser;
    private User otherUser;
    private Role userRole;
    private Pet testPet;
    private MeetUp testMeetUp;

    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setName("ROLE_USER");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRoles(Set.of(userRole));

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");
        otherUser.setRoles(Set.of(userRole));

        testPet = new Pet();
        testPet.setName("Fluffy");
        testPet.setOwner(testUser);

        testMeetUp = new MeetUp();
        testMeetUp.setId(1L);
        testMeetUp.setParticipants(Set.of(testUser, otherUser));
    }

    @Test
    void viewProfile_shouldThrowExceptionWhenPrincipalIsNull() {
        assertThrows(UsernameNotFoundException.class,
                () -> userProfileController.viewProfile("testuser", null, model));
    }

    @Test
    void viewProfile_shouldThrowExceptionWhenLoggedInUserNotFound() {
        when(principal.getName()).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userProfileController.viewProfile("testuser", principal, model));
    }

    @Test
    void viewProfile_shouldThrowExceptionWhenProfileUserNotFound() {
        when(principal.getName()).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userService.getUserByUsername("otheruser")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userProfileController.viewProfile("otheruser", principal, model));
    }

    @Test
    void viewProfile_shouldAddCorrectAttributesForOwnProfile() {
        when(principal.getName()).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(petService.getPetsByOwner(testUser)).thenReturn(List.of(testPet));

        String viewName = userProfileController.viewProfile("testuser", principal, model);

        verify(model).addAttribute("user", testUser);
        verify(model).addAttribute("isOwner", true);
        verify(model).addAttribute("isUser", true);
        verify(model).addAttribute("pets", List.of(testPet));
        assertEquals("profileView", viewName);
    }

    @Test
    void viewProfile_shouldAddCorrectAttributesForOtherProfile() {
        when(principal.getName()).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userService.getUserByUsername("otheruser")).thenReturn(Optional.of(otherUser));
        when(petService.getPetsByOwner(otherUser)).thenReturn(Collections.emptyList());

        String viewName = userProfileController.viewProfile("otheruser", principal, model);

        verify(model).addAttribute("user", otherUser);
        verify(model).addAttribute("isOwner", false);
        verify(model).addAttribute("isUser", true);
        verify(model).addAttribute("pets", Collections.emptyList());
        assertEquals("profileView", viewName);
    }

    @Test
    void viewProfile_shouldHandleNonUserRole() {
        testUser.setRoles(Collections.emptySet());

        when(principal.getName()).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(petService.getPetsByOwner(testUser)).thenReturn(List.of(testPet));

        String viewName = userProfileController.viewProfile("testuser", principal, model);

        verify(model).addAttribute("isUser", false);
        assertEquals("profileView", viewName);
    }

    @Test
    void viewMeetup_shouldThrowExceptionWhenPrincipalIsNull() {
        assertThrows(UsernameNotFoundException.class,
                () -> userProfileController.viewMeetup("testuser", null, model));
    }

    @Test
    void viewMeetup_shouldThrowExceptionWhenLoggedInUserNotFound() {
        when(principal.getName()).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userProfileController.viewMeetup("testuser", principal, model));
    }

    @Test
    void viewMeetup_shouldThrowExceptionWhenProfileUserNotFound() {
        when(principal.getName()).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userService.getUserByUsername("otheruser")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userProfileController.viewMeetup("otheruser", principal, model));
    }

    @Test
    void viewMeetup_shouldAddCorrectAttributesForOwnProfile() {
        testUser.setMeetUps(Set.of(testMeetUp));

        when(principal.getName()).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));

        String viewName = userProfileController.viewMeetup("testuser", principal, model);

        verify(model).addAttribute("user", testUser);
        verify(model).addAttribute("isOwner", true);
        verify(model).addAttribute("meetups", Set.of(testMeetUp));
        assertEquals("viewMeetup", viewName);
    }

    @Test
    void viewMeetup_shouldAddCorrectAttributesForOtherProfile() {
        otherUser.setMeetUps(Set.of(testMeetUp));

        when(principal.getName()).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userService.getUserByUsername("otheruser")).thenReturn(Optional.of(otherUser));

        String viewName = userProfileController.viewMeetup("otheruser", principal, model);

        verify(model).addAttribute("user", otherUser);
        verify(model).addAttribute("isOwner", false);
        verify(model).addAttribute("meetups", Set.of(testMeetUp));
        assertEquals("viewMeetup", viewName);
    }

    @Test
    void viewMeetup_shouldHandleEmptyMeetups() {
        testUser.setMeetUps(Collections.emptySet());

        when(principal.getName()).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));

        String viewName = userProfileController.viewMeetup("testuser", principal, model);

        verify(model).addAttribute("meetups", Collections.emptySet());
        assertEquals("viewMeetup", viewName);
    }
}