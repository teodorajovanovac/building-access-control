package com.buildingaccess.dto.gatepass;

import com.buildingaccess.model.enums.GatePassStatus;

import java.time.LocalDateTime;

/** Javna stranica propusnice (SK, bez prijave) — samo neosetljivi podaci, radi deljenja gostu. */
public record PublicGatePassResponse(
        String code,
        String qrCodeBase64,
        String guestName,
        String reason,
        GatePassStatus status,
        LocalDateTime validFrom,
        LocalDateTime validTo,
        String buildingName
) {
}
