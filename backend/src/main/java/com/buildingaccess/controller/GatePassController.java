package com.buildingaccess.controller;

import com.buildingaccess.dto.common.PageResponse;
import com.buildingaccess.dto.gatepass.GatePassCreateRequest;
import com.buildingaccess.dto.gatepass.GatePassResponse;
import com.buildingaccess.dto.gatepass.GatePassUpdateRequest;
import com.buildingaccess.dto.gatepass.PassStatusHistoryResponse;
import com.buildingaccess.model.User;
import com.buildingaccess.service.GatePassService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/resident/gatepasses")
@Tag(name = "Gate Passes (Resident)", description = "SK3-SK6 Kreiranje/izmena/otkazivanje/pregled propusnica")
public class GatePassController {

    private final GatePassService gatePassService;

    public GatePassController(GatePassService gatePassService) {
        this.gatePassService = gatePassService;
    }

    @GetMapping
    public PageResponse<GatePassResponse> getMine(@AuthenticationPrincipal User currentUser, Pageable pageable) {
        return PageResponse.of(gatePassService.getMine(currentUser, pageable));
    }

    @GetMapping("/{id}")
    public GatePassResponse getById(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        return gatePassService.getById(id, currentUser);
    }

    @GetMapping("/{id}/history")
    public List<PassStatusHistoryResponse> getHistory(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        return gatePassService.getHistory(id, currentUser);
    }

    @PostMapping
    public ResponseEntity<GatePassResponse> create(@Valid @RequestBody GatePassCreateRequest request,
                                                     @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gatePassService.create(request, currentUser));
    }

    @PutMapping("/{id}")
    public GatePassResponse update(@PathVariable Long id, @Valid @RequestBody GatePassUpdateRequest request,
                                    @AuthenticationPrincipal User currentUser) {
        return gatePassService.update(id, request, currentUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        gatePassService.cancel(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
