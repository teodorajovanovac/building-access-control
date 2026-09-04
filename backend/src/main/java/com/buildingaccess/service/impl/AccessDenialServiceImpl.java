package com.buildingaccess.service.impl;

import com.buildingaccess.dto.denial.AccessDenialResponse;
import com.buildingaccess.mapper.AccessDenialMapper;
import com.buildingaccess.model.enums.DenialReasonType;
import com.buildingaccess.repository.AccessDenialRepository;
import com.buildingaccess.service.AccessDenialService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class AccessDenialServiceImpl implements AccessDenialService {

    private final AccessDenialRepository accessDenialRepository;

    public AccessDenialServiceImpl(AccessDenialRepository accessDenialRepository) {
        this.accessDenialRepository = accessDenialRepository;
    }

    /** SK12 — dnevni odbijeni pokušaji zgrade. */
    @Override
    public List<AccessDenialResponse> getToday(Long buildingId) {
        LocalDateTime from = LocalDate.now().atStartOfDay();
        LocalDateTime to = LocalDate.now().atTime(LocalTime.MAX);
        return accessDenialRepository.findByBuildingIdAndAttemptTimeBetweenOrderByAttemptTimeDesc(buildingId, from, to)
                .stream().map(AccessDenialMapper::toResponse).toList();
    }

    /**
     * SK17 — pretraga/filtriranje/sortiranje/paginacija. reasonType stiže kao String iz kontrolera
     * (query parametar), pa ga ovde pretvaramo u enum pre prosleđivanja repozitorijumu.
     */
    @Override
    public Page<AccessDenialResponse> search(Long buildingId, String reasonType, LocalDateTime from, LocalDateTime to,
                                              String text, Pageable pageable) {
        return accessDenialRepository.search(buildingId, toReasonType(reasonType), from, to, blankToNull(text), pageable)
                .map(AccessDenialMapper::toResponse);
    }

    /** Isti filteri kao search, ali bez paginacije — za CSV izvoz kompletnog rezultata pretrage. */
    @Override
    public List<AccessDenialResponse> export(Long buildingId, String reasonType, LocalDateTime from, LocalDateTime to,
                                              String text, Sort sort) {
        return accessDenialRepository.search(buildingId, toReasonType(reasonType), from, to, blankToNull(text), sort)
                .stream().map(AccessDenialMapper::toResponse).toList();
    }

    private DenialReasonType toReasonType(String reasonType) {
        return (reasonType == null || reasonType.isBlank()) ? null : DenialReasonType.valueOf(reasonType);
    }

    private String blankToNull(String text) {
        return (text == null || text.isBlank()) ? null : text;
    }
}
