package com.buildingaccess.dto.apartment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ApartmentRequest(
        @NotBlank String number,
        @Min(0) int floor,
        @NotNull Long buildingId
) {
}
