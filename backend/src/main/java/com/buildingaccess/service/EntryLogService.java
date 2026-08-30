package com.buildingaccess.service;

import com.buildingaccess.dto.entrylog.EntryLogResponse;
import com.buildingaccess.mapper.EntryLogMapper;
import com.buildingaccess.model.EntryLog;
import com.buildingaccess.model.enums.PersonType;
import com.buildingaccess.repository.EntryLogRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EntryLogService {

    private final EntryLogRepository entryLogRepository;

    /** SK12 — dnevna evidencija zgrade (uključuje lica trenutno prisutna, bez evidentiranog izlaska). */
    public List<EntryLogResponse> getToday(Long buildingId) {
        LocalDateTime from = LocalDate.now().atStartOfDay();
        LocalDateTime to = LocalDate.now().atTime(LocalTime.MAX);
        return entryLogRepository.findByBuildingIdAndEntryTimeBetweenOrderByEntryTimeDesc(buildingId, from, to).stream()
                .map(EntryLogMapper::toResponse)
                .toList();
    }

    public List<EntryLogResponse> getCurrentlyPresent(Long buildingId) {
        return entryLogRepository.findByBuildingIdAndExitTimeIsNullOrderByEntryTimeAsc(buildingId).stream()
                .map(EntryLogMapper::toResponse)
                .toList();
    }

    /** SK7 — evidencija ulazaka povezanih sa stanom (sopstveni ulasci + gosti preko propusnica stana). */
    public List<EntryLogResponse> getForApartment(Long apartmentId) {
        return entryLogRepository.findAllForApartment(apartmentId).stream()
                .map(EntryLogMapper::toResponse)
                .toList();
    }

    /** SK17 — pretraga/filtriranje/sortiranje/paginacija. */
    public Page<EntryLogResponse> search(Long buildingId, String personType, LocalDateTime from, LocalDateTime to,
                                          String text, Pageable pageable) {
        Specification<EntryLog> spec = buildSpecification(buildingId, personType, from, to, text);
        return entryLogRepository.findAll(spec, pageable).map(EntryLogMapper::toResponse);
    }

    private Specification<EntryLog> buildSpecification(Long buildingId, String personType, LocalDateTime from,
                                                         LocalDateTime to, String text) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (buildingId != null) {
                predicates.add(cb.equal(root.get("building").get("id"), buildingId));
            }
            if (personType != null && !personType.isBlank()) {
                predicates.add(cb.equal(root.get("personType"), PersonType.valueOf(personType)));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("entryTime"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("entryTime"), to));
            }
            if (text != null && !text.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("personName")), "%" + text.toLowerCase() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
