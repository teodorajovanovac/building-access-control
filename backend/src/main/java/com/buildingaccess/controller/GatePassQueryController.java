package com.buildingaccess.controller;

import com.buildingaccess.dto.common.PageResponse;
import com.buildingaccess.dto.gatepass.GatePassResponse;
import com.buildingaccess.model.enums.GatePassStatus;
import com.buildingaccess.service.GatePassService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/** SK17 — pretraga/filtriranje/sortiranje/paginacija svih propusnica (admin). */
@RestController
@RequestMapping("/api/admin/gatepasses")
@RequiredArgsConstructor
@Tag(name = "Gate Passes (Admin)", description = "SK17 Pregled/pretraga svih propusnica")
public class GatePassQueryController {

    private final GatePassService gatePassService;

    @GetMapping("/search")
    public PageResponse<GatePassResponse> search(@RequestParam(required = false) Long buildingId,
                                                  @RequestParam(required = false) Long apartmentId,
                                                  @RequestParam(required = false) GatePassStatus status,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
                                                  @RequestParam(required = false) String text,
                                                  Pageable pageable) {
        return PageResponse.of(gatePassService.search(buildingId, apartmentId, status, from, to, text, pageable));
    }
}
