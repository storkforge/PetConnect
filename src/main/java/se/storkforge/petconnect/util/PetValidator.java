package se.storkforge.petconnect.util;

import se.storkforge.petconnect.dto.PetInputDTO;
import se.storkforge.petconnect.dto.PetUpdateInputDTO;

import java.util.Arrays;
import java.util.List;

public class PetValidator {

    public static void validatePetInput(PetInputDTO petInput) {
        if (petInput.name() == null || petInput.name().trim().isEmpty()) {
            throw new IllegalArgumentException("Pet name cannot be empty");
        }
        if (petInput.species() == null || petInput.species().trim().isEmpty()) {
            throw new IllegalArgumentException("Pet species cannot be empty");
        }
        if (petInput.age() < 0) {
            throw new IllegalArgumentException("Pet age cannot be negative");
        }
        if (petInput.location() != null && petInput.location().trim().isEmpty()) {
            throw new IllegalArgumentException("Pet location cannot be empty if provided");
        }
        List<String> allowedSpecies = Arrays.asList("dog", "cat", "bird", "rabbit", "fish");
        if (!allowedSpecies.contains(petInput.species().toLowerCase())) {
            throw new IllegalArgumentException("Invalid pet species: " + petInput.species());
        }
    }

    public static void validatePetUpdateInput(PetUpdateInputDTO petUpdate) {
        if(petUpdate.age() != null && petUpdate.age() < 0){
            throw new IllegalArgumentException("Pet age cannot be negative");
        }
    }
}