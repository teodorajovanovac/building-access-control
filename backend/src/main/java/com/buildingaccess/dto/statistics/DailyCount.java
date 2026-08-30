package com.buildingaccess.dto.statistics;

import java.time.LocalDate;

public record DailyCount(
        LocalDate date,
        long entryCount,
        long denialCount
) {
}
