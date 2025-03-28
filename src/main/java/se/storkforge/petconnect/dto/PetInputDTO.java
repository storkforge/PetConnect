package se.storkforge.petconnect.dto;

public record PetInputDTO(
        String name,
        String species,
        boolean available,
        int age,
        String owner,
        String location
) {}
