package com.buildingaccess.dto.gatepass;

import com.buildingaccess.model.enums.GatePassStatus;
import com.buildingaccess.model.enums.GatePassType;

import java.time.LocalDateTime;

public record GatePassResponse(
        Long id,
        String code,
        String qrCodeBase64,
        String guestName,
        String guestPhone,
        String guestEmail,
        String reason,
        LocalDateTime validFrom,
        LocalDateTime validTo,
        int maxEntries,
        int usedEntries,
        GatePassType type,
        GatePassStatus status,
        LocalDateTime createdAt,
        Long apartmentId,
        String apartmentNumber,
        String buildingName,
        Long createdById
) {
}
