package com.buildingaccess.mapper;

import com.buildingaccess.dto.apartment.ApartmentResponse;
import com.buildingaccess.model.Apartment;

public final class ApartmentMapper {

    private ApartmentMapper() {
    }

    public static ApartmentResponse toResponse(Apartment apartment) {
        return new ApartmentResponse(
                apartment.getId(),
                apartment.getNumber(),
                apartment.getFloor(),
                apartment.getBuilding().getId(),
                apartment.getBuilding().getName(),
                apartment.getResidents() != null ? apartment.getResidents().size() : 0
        );
    }
}
