package com.buildingaccess.service;

import com.buildingaccess.dto.statistics.DailyCount;
import com.buildingaccess.dto.statistics.StatisticsResponse;
import com.buildingaccess.model.Building;
import com.buildingaccess.repository.AccessDenialRepository;
import com.buildingaccess.repository.EntryLogRepository;
import com.buildingaccess.repository.GatePassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final BuildingService buildingService;
    private final GatePassRepository gatePassRepository;
    private final EntryLogRepository entryLogRepository;
    private final AccessDenialRepository accessDenialRepository;

    /** SK18 — statistika posećenosti po zgradi i vremenskom periodu. */
    public StatisticsResponse getStatistics(Long buildingId, LocalDate periodFrom, LocalDate periodTo) {
        Building building = buildingService.findEntity(buildingId);
        LocalDateTime from = periodFrom.atStartOfDay();
        LocalDateTime to = periodTo.atTime(LocalTime.MAX);

        long totalEntries = entryLogRepository.countByBuildingIdAndEntryTimeBetween(buildingId, from, to);
        long totalGatePasses = gatePassRepository.countByApartmentBuildingIdAndCreatedAtBetween(buildingId, from, to);
        long totalDenied = accessDenialRepository.countByBuildingIdAndAttemptTimeBetween(buildingId, from, to);
        long currentlyPresent = entryLogRepository.countByBuildingIdAndExitTimeIsNull(buildingId);

        List<DailyCount> dailyBreakdown = new ArrayList<>();
        for (LocalDate day = periodFrom; !day.isAfter(periodTo); day = day.plusDays(1)) {
            LocalDateTime dayStart = day.atStartOfDay();
            LocalDateTime dayEnd = day.atTime(LocalTime.MAX);
            long entries = entryLogRepository.countByBuildingIdAndEntryTimeBetween(buildingId, dayStart, dayEnd);
            long denials = accessDenialRepository.countByBuildingIdAndAttemptTimeBetween(buildingId, dayStart, dayEnd);
            dailyBreakdown.add(new DailyCount(day, entries, denials));
        }

        return new StatisticsResponse(
                building.getId(), building.getName(), periodFrom, periodTo,
                totalEntries, totalGatePasses, totalDenied, currentlyPresent, dailyBreakdown
        );
    }
}
