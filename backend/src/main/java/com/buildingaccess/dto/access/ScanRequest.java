package com.buildingaccess.dto.access;

import jakarta.validation.constraints.NotBlank;

public record ScanRequest(
        @NotBlank String code
) {
}
