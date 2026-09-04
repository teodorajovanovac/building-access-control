package com.buildingaccess.service.impl;

import com.buildingaccess.dto.entrylog.EntryLogResponse;
import com.buildingaccess.mapper.EntryLogMapper;
import com.buildingaccess.model.enums.PersonType;
import com.buildingaccess.repository.EntryLogRepository;
import com.buildingaccess.service.EntryLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class EntryLogServiceImpl implements EntryLogService {

    private final EntryLogRepository entryLogRepository;

    public EntryLogServiceImpl(EntryLogRepository entryLogRepository) {
        this.entryLogRepository = entryLogRepository;
    }

    /** SK12 — dnevna evidencija zgrade (uključuje lica trenutno prisutna, bez evidentiranog izlaska). */
    @Override
    public List<EntryLogResponse> getToday(Long buildingId) {
        LocalDateTime from = LocalDate.now().atStartOfDay();
        LocalDateTime to = LocalDate.now().atTime(LocalTime.MAX);
        return entryLogRepository.findByBuildingIdAndEntryTimeBetweenOrderByEntryTimeDesc(buildingId, from, to).stream()
                .map(EntryLogMapper::toResponse)
                .toList();
    }

    @Override
    public List<EntryLogResponse> getCurrentlyPresent(Long buildingId) {
        return entryLogRepository.findByBuildingIdAndExitTimeIsNullOrderByEntryTimeAsc(buildingId).stream()
                .map(EntryLogMapper::toResponse)
                .toList();
    }

    /** SK7 — evidencija ulazaka povezanih sa stanom (sopstveni ulasci + gosti preko propusnica stana). */
    @Override
    public List<EntryLogResponse> getForApartment(Long apartmentId) {
        return entryLogRepository.findAllForApartment(apartmentId).stream()
                .map(EntryLogMapper::toResponse)
                .toList();
    }

    /**
     * SK17 — pretraga/filtriranje/sortiranje/paginacija. personType stiže kao String iz kontrolera
     * (query parametar), pa ga ovde pretvaramo u enum pre prosleđivanja repozitorijumu.
     */
    @Override
    public Page<EntryLogResponse> search(Long buildingId, String personType, LocalDateTime from, LocalDateTime to,
                                          String text, Pageable pageable) {
        return entryLogRepository.search(buildingId, toPersonType(personType), from, to, blankToNull(text), pageable)
                .map(EntryLogMapper::toResponse);
    }

    /** Isti filteri kao search, ali bez paginacije — za CSV izvoz kompletnog rezultata pretrage. */
    @Override
    public List<EntryLogResponse> export(Long buildingId, String personType, LocalDateTime from, LocalDateTime to,
                                          String text, Sort sort) {
        return entryLogRepository.search(buildingId, toPersonType(personType), from, to, blankToNull(text), sort)
                .stream().map(EntryLogMapper::toResponse).toList();
    }

    private PersonType toPersonType(String personType) {
        return (personType == null || personType.isBlank()) ? null : PersonType.valueOf(personType);
    }

    private String blankToNull(String text) {
        return (text == null || text.isBlank()) ? null : text;
    }
}
