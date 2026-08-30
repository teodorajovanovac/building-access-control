package com.buildingaccess.mapper;

import com.buildingaccess.dto.staff.StaffBadgeResponse;
import com.buildingaccess.model.StaffBadge;
import com.buildingaccess.util.QrCodeUtil;

public final class StaffBadgeMapper {

    private StaffBadgeMapper() {
    }

    public static StaffBadgeResponse toResponse(StaffBadge staffBadge) {
        return new StaffBadgeResponse(
                staffBadge.getId(),
                staffBadge.getFullName(),
                staffBadge.getJobTitle(),
                staffBadge.getBadgeCode(),
                QrCodeUtil.generateBase64Png(staffBadge.getBadgeCode()),
                staffBadge.isActive(),
                staffBadge.getBuilding().getId(),
                staffBadge.getBuilding().getName()
        );
    }
}
