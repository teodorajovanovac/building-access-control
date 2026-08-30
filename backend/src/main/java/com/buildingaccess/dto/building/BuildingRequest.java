package com.buildingaccess.dto.building;

import jakarta.validation.constraints.NotBlank;

public record BuildingRequest(
        @NotBlank String name,
        @NotBlank String address
) {
}
