package com.buildingaccess.dto.building;

public record BuildingResponse(
        Long id,
        String name,
        String address,
        int apartmentCount
) {
}
