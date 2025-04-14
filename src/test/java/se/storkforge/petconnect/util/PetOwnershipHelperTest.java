package se.storkforge.petconnect.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.service.UserService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PetOwnershipHelperTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private PetOwnershipHelper petOwnershipHelper;

    private Pet pet;
    private User owner;
    private User otherUser;
    private Long ownerId = 1L;
    private String currentUsername = "testUser";
    private String otherUsername = "otherUser";

    @BeforeEach
    void setUp() {
        pet = new Pet();
        owner = new User();
        owner.setId(ownerId);
        owner.setUsername(currentUsername);
        otherUser = new User();
        otherUser.setUsername(otherUsername);
    }

    @Test
    void testSetPetOwner_ValidOwner() {
        when(userService.getUserById(ownerId)).thenReturn(owner);

        petOwnershipHelper.setPetOwner(pet, ownerId, currentUsername, userService);

        assertEquals(owner, pet.getOwner());
        assertTrue(owner.getPets().contains(pet));
    }

    @Test
    void testSetPetOwner_InvalidOwnerReference() {
        when(userService.getUserById(ownerId)).thenThrow(new RuntimeException("Invalid owner reference"));

        assertThrows(SecurityException.class, () ->
                petOwnershipHelper.setPetOwner(pet, ownerId, currentUsername, userService));
    }

    @Test
    void testSetPetOwner_OwnershipMismatch() {
        when(userService.getUserById(ownerId)).thenReturn(otherUser);

        assertThrows(SecurityException.class, () ->
                petOwnershipHelper.setPetOwner(pet, ownerId, currentUsername, userService));
    }

    @Test
    void testUpdatePetOwner_ValidTransfer() {
        when(userService.getUserById(ownerId)).thenReturn(owner);
        pet.setOwner(otherUser);
        otherUser.addPet(pet);

        petOwnershipHelper.updatePetOwner(pet, ownerId, currentUsername, userService);

        assertEquals(owner, pet.getOwner());
        assertTrue(owner.getPets().contains(pet));
        assertFalse(otherUser.getPets().contains(pet));
    }

    @Test
    void testUpdatePetOwner_OwnershipTransferMismatch() {
        when(userService.getUserById(ownerId)).thenReturn(otherUser);

        assertThrows(SecurityException.class, () ->
                petOwnershipHelper.updatePetOwner(pet, ownerId, currentUsername, userService));
    }
}