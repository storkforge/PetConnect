package se.storkforge.petconnect.dto;

import se.storkforge.petconnect.entity.User;
import java.util.List;

public record UserResponseDTO(
        Long id,
        String username,
        String email,
        String profilePicturePath,
        List<PetResponseDTO> pets
) {
    public static UserResponseDTO fromEntity(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getProfilePicturePath(),
                user.getPets() != null ?
                        user.getPets().stream().map(PetResponseDTO::fromEntity).toList() :
                        List.of()
        );
    }
}