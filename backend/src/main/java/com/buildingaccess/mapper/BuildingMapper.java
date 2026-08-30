package com.buildingaccess.mapper;

import com.buildingaccess.dto.building.BuildingResponse;
import com.buildingaccess.model.Building;

public final class BuildingMapper {

    private BuildingMapper() {
    }

    public static BuildingResponse toResponse(Building building) {
        return new BuildingResponse(
                building.getId(),
                building.getName(),
                building.getAddress(),
                building.getApartments() != null ? building.getApartments().size() : 0
        );
    }
}
