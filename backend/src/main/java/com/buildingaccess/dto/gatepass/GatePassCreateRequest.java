package com.buildingaccess.dto.gatepass;

import com.buildingaccess.model.enums.GatePassType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record GatePassCreateRequest(
        @NotBlank String guestName,
        String guestPhone,
        @Email String guestEmail,
        @NotBlank String reason,
        @NotNull LocalDateTime validFrom,
        @NotNull LocalDateTime validTo,
        @Min(1) int maxEntries,
        @NotNull GatePassType type
) {
}
