package com.buildingaccess.dto.auth;

import com.buildingaccess.model.enums.Role;

public record AuthResponse(
        String token,
        Long userId,
        String firstName,
        String lastName,
        String email,
        Role role
) {
}
