package com.buildingaccess.controller;

import com.buildingaccess.dto.statistics.StatisticsResponse;
import com.buildingaccess.service.StatisticsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** SK18 — statistika posećenosti po zgradi i vremenskom periodu (admin i obezbeđenje). */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SECURITY')")
@Tag(name = "Statistics", description = "SK18 Pregled statistike posećenosti (dashboard)")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping
    public StatisticsResponse getStatistics(@RequestParam Long buildingId,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return statisticsService.getStatistics(buildingId, from, to);
    }
}
