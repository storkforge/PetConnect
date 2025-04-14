package se.storkforge.petconnect.util;

import org.junit.jupiter.api.Test;
import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.entity.User;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OwnershipValidatorTest {

    @Test
    void validateOwnership_shouldNotThrowException_whenOwnerMatchesUsername() {
        Pet pet = mock(Pet.class);
        User owner = mock(User.class);
        when(pet.getOwner()).thenReturn(owner);
        when(owner.getUsername()).thenReturn("testUser");

        assertDoesNotThrow(() -> OwnershipValidator.validateOwnership(pet, "testUser"));
    }

    @Test
    void validateOwnership_shouldThrowException_whenOwnerDoesNotMatchUsername() {
        Pet pet = mock(Pet.class);
        User owner = mock(User.class);
        when(pet.getOwner()).thenReturn(owner);
        when(owner.getUsername()).thenReturn("anotherUser");

        assertThrows(SecurityException.class, () -> OwnershipValidator.validateOwnership(pet, "testUser"));
    }

    @Test
    void validateOwnership_shouldThrowException_whenOwnerIsNull() {
        Pet pet = mock(Pet.class);
        when(pet.getOwner()).thenReturn(null);

        assertThrows(SecurityException.class, () -> OwnershipValidator.validateOwnership(pet, "testUser"));
    }
}