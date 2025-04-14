package se.storkforge.petconnect.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PetRequestDTO(
        @NotBlank @Size(min = 2, max = 50) String name,
        @NotBlank @Size(max = 50) String species,
        boolean available,
        @Min(0) int age,
        Long ownerId,
        String location
) {}
