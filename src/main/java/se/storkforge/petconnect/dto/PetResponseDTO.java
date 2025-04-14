package se.storkforge.petconnect.dto;

import se.storkforge.petconnect.entity.Pet;

public record PetResponseDTO(
        Long id,
        String name,
        String species,
        boolean available,
        int age,
        Long ownerId,
        String ownerUsername,
        String location,
        String profilePicturePath
) {
    public static PetResponseDTO fromEntity(Pet pet) {
        return new PetResponseDTO(
                pet.getId(),
                pet.getName(),
                pet.getSpecies(),
                pet.isAvailable(),
                pet.getAge(),
                pet.getOwner() != null ? pet.getOwner().getId() : null,
                pet.getOwner() != null ? pet.getOwner().getUsername() : null,
                pet.getLocation(),
                pet.getProfilePicturePath()
        );
    }
}