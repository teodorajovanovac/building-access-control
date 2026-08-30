package com.buildingaccess.dto.user;

import jakarta.validation.constraints.NotBlank;

/** Admin izmena postojećeg korisnika — dodela/promena zgrade (SECURITY) ili stana (RESIDENT). */
public record UserUpdateRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        Long apartmentId,
        Long buildingId
) {
}
