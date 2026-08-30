package com.buildingaccess.controller;

import com.buildingaccess.dto.user.UserResponse;
import com.buildingaccess.model.User;
import com.buildingaccess.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Stanareva sopstvena stranica sa ličnim bedž kodom i QR-om (SK2) — bez ovoga stanar
 * nema način da vidi svoj badgeCode/QR nakon registracije osim preko admina.
 * Korisnik se svežе učitava preko servisa (a ne direktno iz @AuthenticationPrincipal)
 * jer je principal objekat iz JWT filtera van Hibernate sesije kontrolera — lazy
 * apartment/building bi pukli sa "no Session".
 */
@RestController
@RequestMapping("/api/resident/me")
@RequiredArgsConstructor
@PreAuthorize("hasRole('RESIDENT')")
@Tag(name = "Resident profile", description = "SK2 — lični bedž kod i QR stanara")
public class ResidentProfileController {

    private final UserService userService;

    @GetMapping
    public UserResponse me(@AuthenticationPrincipal User currentUser) {
        return userService.getById(currentUser.getId());
    }
}
