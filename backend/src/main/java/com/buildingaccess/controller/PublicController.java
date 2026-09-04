package com.buildingaccess.controller;

import com.buildingaccess.dto.apartment.ApartmentResponse;
import com.buildingaccess.dto.building.BuildingResponse;
import com.buildingaccess.dto.gatepass.PublicGatePassResponse;
import com.buildingaccess.service.ApartmentService;
import com.buildingaccess.service.BuildingService;
import com.buildingaccess.service.GatePassService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Javno dostupni podaci bez prijave: stranica propusnice (deljenje gostu), i lista
 * zgrada/stanova — potrebna stanaru da izabere svoj stan na formi registracije (SK2),
 * pošto pre registracije još nema token za /api/admin/**.
 */
@RestController
@RequestMapping("/api/public")
@Tag(name = "Public", description = "Javna stranica propusnice i lista zgrada/stanova za registraciju (bez prijave)")
public class PublicController {

    private final GatePassService gatePassService;
    private final BuildingService buildingService;
    private final ApartmentService apartmentService;

    public PublicController(GatePassService gatePassService,
                             BuildingService buildingService,
                             ApartmentService apartmentService) {
        this.gatePassService = gatePassService;
        this.buildingService = buildingService;
        this.apartmentService = apartmentService;
    }

    @GetMapping("/gatepass/{code}")
    public PublicGatePassResponse getGatePass(@PathVariable String code) {
        return gatePassService.getPublicByCode(code);
    }

    @GetMapping("/buildings")
    public List<BuildingResponse> getBuildings() {
        return buildingService.getAll();
    }

    @GetMapping("/buildings/{buildingId}/apartments")
    public List<ApartmentResponse> getApartments(@PathVariable Long buildingId) {
        return apartmentService.getByBuilding(buildingId);
    }
}
