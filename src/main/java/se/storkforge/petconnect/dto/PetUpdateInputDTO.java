package se.storkforge.petconnect.dto;

public record PetUpdateInputDTO(
        String name,
        String species,
        Boolean available,
        Integer age,
        String owner,
        String location
) {}