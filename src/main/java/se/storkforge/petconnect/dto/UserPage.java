package se.storkforge.petconnect.dto;

import se.storkforge.petconnect.entity.User; // Assuming User entity is in this package or accessible

import java.util.List;

public record UserPage(
        List<User> content,
        PaginationInfo pageInfo
) {}