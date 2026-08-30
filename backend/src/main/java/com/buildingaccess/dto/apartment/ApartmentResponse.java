package com.buildingaccess.dto.apartment;

public record ApartmentResponse(
        Long id,
        String number,
        int floor,
        Long buildingId,
        String buildingName,
        int residentCount
) {
}
