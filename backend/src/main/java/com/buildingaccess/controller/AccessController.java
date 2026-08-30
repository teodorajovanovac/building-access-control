package com.buildingaccess.controller;

import com.buildingaccess.dto.access.ManualDenyRequest;
import com.buildingaccess.dto.access.PersonSearchResponse;
import com.buildingaccess.dto.access.ScanRequest;
import com.buildingaccess.dto.access.ScanResultResponse;
import com.buildingaccess.dto.user.UserResponse;
import com.buildingaccess.model.User;
import com.buildingaccess.model.enums.PersonType;
import com.buildingaccess.service.AccessProcessingService;
import com.buildingaccess.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/security")
@RequiredArgsConstructor
@Tag(name = "Access Processing (Security)", description = "SK8-SK11 Obrada dolaska, ručna pretraga i odbijanje ulaska")
public class AccessController {

    private final AccessProcessingService accessProcessingService;
    private final UserService userService;

    /** Obezbeđenju treba sopstvena zgrada (id) da bi pozvalo npr. statistiku bez ručnog unosa. */
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal User security) {
        return userService.getById(security.getId());
    }

    /** Jedinstveni ulaz za sve dolaske — sistem sam prepoznaje propusnicu / lični kod stanara / kod osoblja. */
    @PostMapping("/access/scan")
    public ScanResultResponse scan(@Valid @RequestBody ScanRequest request, @AuthenticationPrincipal User security) {
        return accessProcessingService.processScan(request.code(), security);
    }

    @GetMapping("/people/search")
    public List<PersonSearchResponse> searchPeople(@RequestParam String query, @AuthenticationPrincipal User security) {
        return accessProcessingService.searchPeople(query, security);
    }

    @PostMapping("/access/manual/{type}/{id}")
    public ScanResultResponse processManual(@PathVariable PersonType type, @PathVariable Long id,
                                             @AuthenticationPrincipal User security) {
        return accessProcessingService.processManual(type, id, security);
    }

    @PostMapping("/access/deny")
    public ScanResultResponse deny(@Valid @RequestBody ManualDenyRequest request, @AuthenticationPrincipal User security) {
        return accessProcessingService.manualDeny(request, security);
    }
}
