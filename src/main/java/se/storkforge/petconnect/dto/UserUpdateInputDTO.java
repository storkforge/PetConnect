package se.storkforge.petconnect.dto;
public record UserUpdateInputDTO(
        String username,
        String email,
        String password
) {}