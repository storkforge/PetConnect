package se.storkforge.petconnect.dto;

public record PetInputDTO(
        String name,
        String species,
        boolean available,
        int age,
        Long ownerId,  
        String location
) {}