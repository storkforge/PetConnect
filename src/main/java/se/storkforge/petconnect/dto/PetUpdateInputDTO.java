package se.storkforge.petconnect.dto;

public record PetUpdateInputDTO(
        String name,
        String species,
        Boolean available,
        Integer age,
        Long ownerId,
        String location
) {}