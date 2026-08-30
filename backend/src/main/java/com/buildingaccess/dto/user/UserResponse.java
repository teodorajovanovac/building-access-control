package com.buildingaccess.dto.user;

import com.buildingaccess.model.enums.Role;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        Role role,
        String badgeCode,
        String qrCodeBase64,
        Long apartmentId,
        String apartmentNumber,
        Long buildingId,
        String buildingName,
        LocalDateTime createdAt
) {
}
