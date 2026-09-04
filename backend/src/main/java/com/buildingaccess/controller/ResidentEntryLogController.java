package com.buildingaccess.controller;

import com.buildingaccess.dto.entrylog.EntryLogResponse;
import com.buildingaccess.model.User;
import com.buildingaccess.service.EntryLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/resident/entrylogs")
@Tag(name = "Entry Logs (Resident)", description = "SK7 Pregled evidencije ulazaka za svoj stan")
public class ResidentEntryLogController {

    private final EntryLogService entryLogService;

    public ResidentEntryLogController(EntryLogService entryLogService) {
        this.entryLogService = entryLogService;
    }

    @GetMapping
    public List<EntryLogResponse> getForMyApartment(@AuthenticationPrincipal User currentUser) {
        return entryLogService.getForApartment(currentUser.getApartment().getId());
    }
}
