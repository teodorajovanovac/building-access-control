package com.buildingaccess.dto.denial;

import com.buildingaccess.model.enums.DenialReasonType;

import java.time.LocalDateTime;

public record AccessDenialResponse(
        Long id,
        String enteredCode,
        String personName,
        LocalDateTime attemptTime,
        DenialReasonType reasonType,
        String reasonNote,
        String buildingName,
        String processedByName,
        String gatePassCode
) {
}
