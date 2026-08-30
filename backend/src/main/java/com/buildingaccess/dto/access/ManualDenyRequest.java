package com.buildingaccess.dto.access;

import jakarta.validation.constraints.NotBlank;

/** SK11 ručni slučaj — reasonNote je uvek obavezan (i DTO i service-nivo validacija). code je opcion. */
public record ManualDenyRequest(
        String code,
        String personName,
        @NotBlank(message = "Razlog odbijanja je obavezan") String reasonNote
) {
}
