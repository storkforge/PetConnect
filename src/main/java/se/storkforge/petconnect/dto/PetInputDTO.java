package se.storkforge.petconnect.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record PetInputDTO(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Species is required")
        String species,

        boolean available,

        @Min(value = 0, message = "Age cannot be negative")
        int age,

        Long ownerId,

        String location
) {}