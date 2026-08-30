package com.buildingaccess.dto.gatepass;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/** SK4 — dozvoljeno samo dok je propusnica ACTIVE (proverava se u service sloju). */
public record GatePassUpdateRequest(
        @NotBlank String guestName,
        String guestPhone,
        @Email String guestEmail,
        @NotBlank String reason,
        @NotNull LocalDateTime validFrom,
        @NotNull LocalDateTime validTo,
        @Min(1) int maxEntries
) {
}
