package com.buildingaccess.mapper;

import com.buildingaccess.dto.user.UserResponse;
import com.buildingaccess.model.User;
import com.buildingaccess.util.QrCodeUtil;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getBadgeCode(),
                user.getBadgeCode() != null ? QrCodeUtil.generateBase64Png(user.getBadgeCode()) : null,
                user.getApartment() != null ? user.getApartment().getId() : null,
                user.getApartment() != null ? user.getApartment().getNumber() : null,
                user.getBuilding() != null ? user.getBuilding().getId()
                        : user.getApartment() != null ? user.getApartment().getBuilding().getId() : null,
                user.getBuilding() != null ? user.getBuilding().getName()
                        : user.getApartment() != null ? user.getApartment().getBuilding().getName() : null,
                user.getJobTitle(),
                user.getCreatedAt()
        );
    }
}
