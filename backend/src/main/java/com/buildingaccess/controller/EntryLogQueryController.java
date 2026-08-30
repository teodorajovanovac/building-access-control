package com.buildingaccess.controller;

import com.buildingaccess.dto.common.PageResponse;
import com.buildingaccess.dto.entrylog.EntryLogResponse;
import com.buildingaccess.model.User;
import com.buildingaccess.model.enums.Role;
import com.buildingaccess.service.EntryLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Deljeni upiti nad evidencijom ulazaka — SK12 (dnevna evidencija obezbeđenja) i SK17
 * (pretraga/filtriranje/sortiranje/paginacija za admina i obezbeđenje).
 */
@RestController
@RequestMapping("/api/query/entrylogs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SECURITY')")
@Tag(name = "Entry Logs (query)", description = "SK12 Dnevna evidencija, SK17 Pretraga/filtriranje/sortiranje/paginacija")
public class EntryLogQueryController {

    private final EntryLogService entryLogService;

    @GetMapping("/today")
    public List<EntryLogResponse> today(@RequestParam(required = false) Long buildingId,
                                         @AuthenticationPrincipal User currentUser) {
        return entryLogService.getToday(resolveBuildingId(buildingId, currentUser));
    }

    @GetMapping("/present")
    public List<EntryLogResponse> currentlyPresent(@RequestParam(required = false) Long buildingId,
                                                     @AuthenticationPrincipal User currentUser) {
        return entryLogService.getCurrentlyPresent(resolveBuildingId(buildingId, currentUser));
    }

    @GetMapping("/search")
    public PageResponse<EntryLogResponse> search(@RequestParam(required = false) Long buildingId,
                                                  @RequestParam(required = false) String personType,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
                                                  @RequestParam(required = false) String text,
                                                  @AuthenticationPrincipal User currentUser,
                                                  Pageable pageable) {
        Long effectiveBuildingId = currentUser.getRole() == Role.SECURITY
                ? currentUser.getBuilding().getId()
                : buildingId;
        return PageResponse.of(entryLogService.search(effectiveBuildingId, personType, from, to, text, pageable));
    }

    private Long resolveBuildingId(Long requested, User currentUser) {
        if (currentUser.getRole() == Role.SECURITY) {
            return currentUser.getBuilding().getId();
        }
        if (requested == null) {
            throw new IllegalArgumentException("buildingId je obavezan parametar za admina");
        }
        return requested;
    }
}
