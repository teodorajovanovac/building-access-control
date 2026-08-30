package com.buildingaccess.controller;

import com.buildingaccess.dto.apartment.ApartmentRequest;
import com.buildingaccess.dto.apartment.ApartmentResponse;
import com.buildingaccess.service.ApartmentService;
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
@RequestMapping("/api/admin/apartments")
@RequiredArgsConstructor
@Tag(name = "Apartments (Admin)", description = "SK14 Upravljanje stanovima (CRUD)")
public class ApartmentController {

    private final ApartmentService apartmentService;

    @GetMapping
    public List<ApartmentResponse> getByBuilding(@RequestParam Long buildingId) {
        return apartmentService.getByBuilding(buildingId);
    }

    @GetMapping("/{id}")
    public ApartmentResponse getById(@PathVariable Long id) {
        return apartmentService.getById(id);
    }

    @PostMapping
    public ResponseEntity<ApartmentResponse> create(@Valid @RequestBody ApartmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(apartmentService.create(request));
    }

    @PutMapping("/{id}")
    public ApartmentResponse update(@PathVariable Long id, @Valid @RequestBody ApartmentRequest request) {
        return apartmentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        apartmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
