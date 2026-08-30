package com.buildingaccess.controller;

import com.buildingaccess.dto.staff.StaffBadgeRequest;
import com.buildingaccess.dto.staff.StaffBadgeResponse;
import com.buildingaccess.dto.staff.StaffBadgeUpdateRequest;
import com.buildingaccess.service.StaffBadgeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/staff")
@RequiredArgsConstructor
@Tag(name = "Staff (Admin)", description = "SK16 Upravljanje osobljem zgrade (CRUD + bedž kod)")
public class StaffBadgeController {

    private final StaffBadgeService staffBadgeService;

    @GetMapping
    public List<StaffBadgeResponse> getByBuilding(@RequestParam Long buildingId) {
        return staffBadgeService.getByBuilding(buildingId);
    }

    @GetMapping("/{id}")
    public StaffBadgeResponse getById(@PathVariable Long id) {
        return staffBadgeService.getById(id);
    }

    @PostMapping
    public ResponseEntity<StaffBadgeResponse> create(@Valid @RequestBody StaffBadgeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(staffBadgeService.create(request));
    }

    @PutMapping("/{id}")
    public StaffBadgeResponse update(@PathVariable Long id, @Valid @RequestBody StaffBadgeUpdateRequest request) {
        return staffBadgeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        staffBadgeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
