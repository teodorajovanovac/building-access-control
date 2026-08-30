package com.buildingaccess.controller;

import com.buildingaccess.dto.gatepass.PublicGatePassResponse;
import com.buildingaccess.service.GatePassService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Javna stranica propusnice — bez prijave, samo neosetljivi podaci, radi lakšeg deljenja gostu. */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@Tag(name = "Public", description = "Javna stranica propusnice (bez prijave)")
public class PublicController {

    private final GatePassService gatePassService;

    @GetMapping("/gatepass/{code}")
    public PublicGatePassResponse getGatePass(@PathVariable String code) {
        return gatePassService.getPublicByCode(code);
    }
}
