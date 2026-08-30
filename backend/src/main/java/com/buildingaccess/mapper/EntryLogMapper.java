package com.buildingaccess.mapper;

import com.buildingaccess.dto.entrylog.EntryLogResponse;
import com.buildingaccess.model.EntryLog;

public final class EntryLogMapper {

    private EntryLogMapper() {
    }

    public static EntryLogResponse toResponse(EntryLog entryLog) {
        return new EntryLogResponse(
                entryLog.getId(),
                entryLog.getPersonType(),
                entryLog.getPersonName(),
                entryLog.getEntryTime(),
                entryLog.getExitTime(),
                entryLog.isManualEntry(),
                entryLog.getNote(),
                entryLog.getBuilding().getName(),
                entryLog.getProcessedBy().getFirstName() + " " + entryLog.getProcessedBy().getLastName(),
                entryLog.getGatePass() != null ? entryLog.getGatePass().getCode() : null
        );
    }
}
