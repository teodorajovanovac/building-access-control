package com.buildingaccess.dto.entrylog;

import com.buildingaccess.model.enums.PersonType;

import java.time.LocalDateTime;

public record EntryLogResponse(
        Long id,
        PersonType personType,
        String personName,
        LocalDateTime entryTime,
        LocalDateTime exitTime,
        boolean manualEntry,
        String note,
        String buildingName,
        String processedByName,
        String gatePassCode
) {
}
