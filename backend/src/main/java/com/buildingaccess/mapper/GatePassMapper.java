package com.buildingaccess.mapper;

import com.buildingaccess.dto.gatepass.GatePassResponse;
import com.buildingaccess.dto.gatepass.PassStatusHistoryResponse;
import com.buildingaccess.dto.gatepass.PublicGatePassResponse;
import com.buildingaccess.model.GatePass;
import com.buildingaccess.model.PassStatusHistory;
import com.buildingaccess.util.QrCodeUtil;

public final class GatePassMapper {

    private GatePassMapper() {
    }

    public static GatePassResponse toResponse(GatePass gatePass) {
        return new GatePassResponse(
                gatePass.getId(),
                gatePass.getCode(),
                QrCodeUtil.generateBase64Png(gatePass.getCode()),
                gatePass.getGuestName(),
                gatePass.getGuestPhone(),
                gatePass.getGuestEmail(),
                gatePass.getReason(),
                gatePass.getValidFrom(),
                gatePass.getValidTo(),
                gatePass.getMaxEntries(),
                gatePass.getUsedEntries(),
                gatePass.getType(),
                gatePass.getStatus(),
                gatePass.getCreatedAt(),
                gatePass.getApartment().getId(),
                gatePass.getApartment().getNumber(),
                gatePass.getApartment().getBuilding().getName(),
                gatePass.getCreatedBy().getId()
        );
    }

    public static PublicGatePassResponse toPublicResponse(GatePass gatePass) {
        return new PublicGatePassResponse(
                gatePass.getCode(),
                QrCodeUtil.generateBase64Png(gatePass.getCode()),
                gatePass.getGuestName(),
                gatePass.getReason(),
                gatePass.getStatus(),
                gatePass.getValidFrom(),
                gatePass.getValidTo(),
                gatePass.getApartment().getBuilding().getName()
        );
    }

    public static PassStatusHistoryResponse toHistoryResponse(PassStatusHistory history) {
        return new PassStatusHistoryResponse(
                history.getId(),
                history.getPreviousStatus(),
                history.getNewStatus(),
                history.getChangedAt(),
                history.getChangedBy() != null
                        ? history.getChangedBy().getFirstName() + " " + history.getChangedBy().getLastName()
                        : "Sistem"
        );
    }
}
