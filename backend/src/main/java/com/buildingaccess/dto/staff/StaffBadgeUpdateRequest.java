package com.buildingaccess.dto.staff;

import jakarta.validation.constraints.NotBlank;

public record StaffBadgeUpdateRequest(
        @NotBlank String fullName,
        @NotBlank String jobTitle,
        boolean active
) {
}
