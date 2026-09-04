package com.buildingaccess.controller;

import com.buildingaccess.dto.user.UserResponse;
import com.buildingaccess.model.User;
import com.buildingaccess.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Korisnik se svežе učitava preko servisa jer je principal van Hibernate sesije — lazy polja bi pukla. */
@RestController
@RequestMapping("/api/resident/me")
@PreAuthorize("hasRole('RESIDENT')")
@Tag(name = "Resident profile", description = "SK2 — lični bedž kod i QR stanara")
public class ResidentProfileController {

    private final UserService userService;

    public ResidentProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public UserResponse me(@AuthenticationPrincipal User currentUser) {
        return userService.getById(currentUser.getId());
    }
}
