package com.buildingaccess.dto.staff;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StaffBadgeRequest(
        @NotBlank String fullName,
        @NotBlank String jobTitle,
        @NotNull Long buildingId
) {
}
