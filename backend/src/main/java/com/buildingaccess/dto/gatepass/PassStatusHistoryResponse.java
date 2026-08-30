package com.buildingaccess.dto.gatepass;

import com.buildingaccess.model.enums.GatePassStatus;

import java.time.LocalDateTime;

public record PassStatusHistoryResponse(
        Long id,
        GatePassStatus previousStatus,
        GatePassStatus newStatus,
        LocalDateTime changedAt,
        String changedByName
) {
}
