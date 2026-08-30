package com.buildingaccess.dto.access;

import com.buildingaccess.model.enums.PersonType;

public record PersonSearchResponse(
        Long id,
        PersonType personType,
        String fullName,
        String apartmentNumber,
        String jobTitle,
        boolean currentlyInBuilding
) {
}
