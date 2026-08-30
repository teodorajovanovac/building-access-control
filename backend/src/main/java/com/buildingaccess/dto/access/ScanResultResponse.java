package com.buildingaccess.dto.access;

import com.buildingaccess.model.enums.PersonType;

import java.time.LocalDateTime;

public record ScanResultResponse(
        ScanOutcome outcome,
        String message,
        PersonType personType,
        String personName,
        String gatePassCode,
        LocalDateTime entryTime,
        LocalDateTime exitTime
) {
}
