package se.storkforge.petconnect.dto;
public record PaginationInfo(
        int totalElements,
        int totalPages,
        int currentPage,
        int pageSize
) {}