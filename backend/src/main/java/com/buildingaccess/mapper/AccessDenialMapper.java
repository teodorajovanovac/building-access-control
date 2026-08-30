package com.buildingaccess.mapper;

import com.buildingaccess.dto.denial.AccessDenialResponse;
import com.buildingaccess.model.AccessDenial;

public final class AccessDenialMapper {

    private AccessDenialMapper() {
    }

    public static AccessDenialResponse toResponse(AccessDenial denial) {
        return new AccessDenialResponse(
                denial.getId(),
                denial.getEnteredCode(),
                denial.getPersonName(),
                denial.getAttemptTime(),
                denial.getReasonType(),
                denial.getReasonNote(),
                denial.getBuilding().getName(),
                denial.getProcessedBy().getFirstName() + " " + denial.getProcessedBy().getLastName(),
                denial.getGatePass() != null ? denial.getGatePass().getCode() : null
        );
    }
}
