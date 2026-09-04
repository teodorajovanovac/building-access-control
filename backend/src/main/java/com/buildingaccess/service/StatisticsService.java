package com.buildingaccess.service;

import com.buildingaccess.dto.statistics.StatisticsResponse;

import java.time.LocalDate;

public interface StatisticsService {

    /** SK18 — statistika posećenosti po zgradi i vremenskom periodu. */
    StatisticsResponse getStatistics(Long buildingId, LocalDate periodFrom, LocalDate periodTo);
}
