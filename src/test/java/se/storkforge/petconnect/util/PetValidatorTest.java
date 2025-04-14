package se.storkforge.petconnect.util;

import org.junit.jupiter.api.Test;
import se.storkforge.petconnect.dto.PetInputDTO;
import se.storkforge.petconnect.dto.PetUpdateInputDTO;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PetValidatorTest {

    @Test
    void testValidatePetInput_ValidInput() {
        PetInputDTO validInput = new PetInputDTO("Buddy", "Dog", true, 3, 1L, "New York");
        assertDoesNotThrow(() -> PetValidator.validatePetInput(validInput));
    }

    @Test
    void testValidatePetInput_EmptyName() {
        PetInputDTO invalidInput = new PetInputDTO("", "Dog", true, 3, 1L, "New York");
        assertThrows(IllegalArgumentException.class, () -> PetValidator.validatePetInput(invalidInput));
    }

    @Test
    void testValidatePetInput_NullName() {
        PetInputDTO invalidInput = new PetInputDTO(null, "Dog", true, 3, 1L, "New York");
        assertThrows(IllegalArgumentException.class, () -> PetValidator.validatePetInput(invalidInput));
    }

    @Test
    void testValidatePetInput_EmptySpecies() {
        PetInputDTO invalidInput = new PetInputDTO("Buddy", "", true, 3, 1L, "New York");
        assertThrows(IllegalArgumentException.class, () -> PetValidator.validatePetInput(invalidInput));
    }

    @Test
    void testValidatePetInput_NullSpecies() {
        PetInputDTO invalidInput = new PetInputDTO("Buddy", null, true, 3, 1L, "New York");
        assertThrows(IllegalArgumentException.class, () -> PetValidator.validatePetInput(invalidInput));
    }

    @Test
    void testValidatePetInput_NegativeAge() {
        PetInputDTO invalidInput = new PetInputDTO("Buddy", "Dog", true, -1, 1L, "New York");
        assertThrows(IllegalArgumentException.class, () -> PetValidator.validatePetInput(invalidInput));
    }

    @Test
    void testValidatePetInput_EmptyLocation() {
        PetInputDTO invalidInput = new PetInputDTO("Buddy", "Dog", true, 3, 1L, "");
        assertThrows(IllegalArgumentException.class, () -> PetValidator.validatePetInput(invalidInput));
    }

    @Test
    void testValidatePetInput_InvalidSpecies() {
        PetInputDTO invalidInput = new PetInputDTO("Buddy", "Elephant", true, 3, 1L, "New York");
        assertThrows(IllegalArgumentException.class, () -> PetValidator.validatePetInput(invalidInput));
    }

    @Test
    void testValidatePetUpdateInput_ValidInput() {
        PetUpdateInputDTO validInput = new PetUpdateInputDTO("Updated Name", "Cat", false, 5, 2L, "Updated Location");
        assertDoesNotThrow(() -> PetValidator.validatePetUpdateInput(validInput));
    }

    @Test
    void testValidatePetUpdateInput_NegativeAge() {
        PetUpdateInputDTO invalidInput = new PetUpdateInputDTO("Updated Name", "Cat", false, -1, 2L, "Updated Location");
        assertThrows(IllegalArgumentException.class, () -> PetValidator.validatePetUpdateInput(invalidInput));
    }
}