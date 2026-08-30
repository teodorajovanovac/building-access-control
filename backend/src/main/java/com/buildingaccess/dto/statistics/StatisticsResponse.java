package com.buildingaccess.dto.statistics;

import java.time.LocalDate;
import java.util.List;

public record StatisticsResponse(
        Long buildingId,
        String buildingName,
        LocalDate periodFrom,
        LocalDate periodTo,
        long totalEntries,
        long totalGatePasses,
        long totalDeniedAttempts,
        long currentlyPresentCount,
        List<DailyCount> dailyBreakdown
) {
}
