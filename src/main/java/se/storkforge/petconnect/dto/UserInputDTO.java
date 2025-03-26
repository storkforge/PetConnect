package se.storkforge.petconnect.dto;

// src/main/java/se/storkforge/petconnect/dto/UserInputDTO.java
public record UserInputDTO(
        String username,
        String email,
        String password
) {}