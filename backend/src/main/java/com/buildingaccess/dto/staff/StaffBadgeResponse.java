package com.buildingaccess.dto.staff;

public record StaffBadgeResponse(
        Long id,
        String fullName,
        String jobTitle,
        String badgeCode,
        String qrCodeBase64,
        boolean active,
        Long buildingId,
        String buildingName
) {
}
